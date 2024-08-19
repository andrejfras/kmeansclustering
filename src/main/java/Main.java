import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.swing.*;
import javax.swing.event.MouseInputListener;

import mpi.Intracomm;
import mpi.MPI;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.cache.FileBasedLocalCache;
import org.jxmapviewer.input.CenterMapListener;
import org.jxmapviewer.input.PanKeyListener;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCenter;
import org.jxmapviewer.painter.CompoundPainter;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.TileFactoryInfo;
import org.jxmapviewer.viewer.WaypointPainter;

public class Main {
    private static final ScheduledExecutorService kmeansExecutor = Executors.newScheduledThreadPool(2);

    public static void main(String[] args) {
        // parses command-line arguments
        //  available arguments:
        //      --clusters <number> ; number of clusters to use during execution
        //      --sites <number>    ; number of sites to use during execution
        //      --cycles <number>   ; number of cycles, how many times the K-means computation will be done
        //      --sequential        ; run the computation in sequential mode
        //      --parallel          ; run the computation in parallel mode
        //      --distributed       ; run the computation in sequential mode
        //
        //  --sequential, --parallel, --distributed can't be used at the same time
        Map<String, Integer> params = parseArgs(args);

        // initialize the distributed worker communication
        //  this won't be used if not running in --distributed mode
        MPI.Init(args);
        Intracomm comm = MPI.COMM_WORLD;

        boolean gui = false;
        int rank = comm.Rank();
        int size = comm.Size();

        // ensure all distributed workers have the same random generator
        long[] seed = new long[] { System.nanoTime() };
        Random random;
        if (rank == 0) {
            for (int i = 1; i < size; i++) {
                comm.Send(seed, 0, 1, MPI.LONG, i, 20);
            }
        } else {
            comm.Recv(seed, 0, 1, MPI.LONG, 0, 20);
        }

        random = new Random(seed[0]);

        // number of clusters
        int clusterCount = params.getOrDefault("clusters", 4);
        // number od sites
        int sitesCount = params.getOrDefault("sites", 4000);

        // number of cycles
        int cycles = params.getOrDefault("cycles", 40);

        // Create sites from the geo-positions
        Set<AccumulationSite> sites = new HashSet<>();
        List<Point> sitePoints = new ArrayList<>();
        Set<ProcessingPlant> plants = new HashSet<>();

        // generate sites
        PointGenerator pointGenerator = new PointGenerator(random, sitesCount);
        for (Point point : pointGenerator.getPoints()) {
            sites.add(new AccumulationSite(Color.RED, new GeoPosition(point.getLa(), point.getLo())));
            sitePoints.add(point);
        }

        // generate clusters
        PlantGenerator plantGenerator = new PlantGenerator(random, pointGenerator.getPoints(), clusterCount);
        plantGenerator.generatePlants();

        // if this is not the main runner, ignore further code (code that includes UI and K-means runners)
        //  when the program is not running in the distributed mode, this will just make the program exit instantly
        //      since it's not needed for sequential and parallel modes
        //  when the program is in distributed mode, this will run the worker, which does computation requested
        //      by the main runner program and responds to ti
        if (rank != 0) {
            if (!params.containsKey("distributed"))
                return;

            KMeansDistributedWorker worker = new KMeansDistributedWorker(clusterCount, plantGenerator.getPointsOfPlants());
            worker.run();
            return;
        }

        // get instance of K-means based on command-line arguments
        IKMeans kMeans;
        if (params.containsKey("sequential"))
            kMeans = new KMeans(clusterCount, sitePoints, plantGenerator.getPointsOfPlants());
        else if (params.containsKey("parallel"))
            kMeans = new KMeansMultithreaded(clusterCount, sitePoints, plantGenerator.getPointsOfPlants());
        else if (params.containsKey("distributed"))
            kMeans = new KMeansDistributed(clusterCount, sitePoints, plantGenerator.getPointsOfPlants(), size);
        else {
            System.out.println("No mode specified, defaulting to sequential");
            kMeans = new KMeans(clusterCount, sitePoints, plantGenerator.getPointsOfPlants());
        }

        plantGenerator.updatePlants(kMeans.getCentroids());

        // a couple of lines to handle logging UI errors more nicely
        //  otherwise console will be spammed
        try {
            SwingUtilities.invokeAndWait(() -> {
                Thread.currentThread().setUncaughtExceptionHandler((thread, throwable) -> {
                    System.out.println("Exception in \"" + thread.getName() + "\": " + throwable.getMessage());
                });
            });
        } catch (Exception e) { /* ignored */}


        // Create a TileFactoryInfo for OSM
        TileFactoryInfo info = new OSMTileFactoryInfo();
        DefaultTileFactory tileFactory = new DefaultTileFactory(info);
        tileFactory.setThreadPoolSize(1);

        // Setup local file cache
        File cacheDire = new File(System.getProperty("user.home") + File.separator + ".jxmapviewer2");
        tileFactory.setLocalCache(new FileBasedLocalCache(cacheDire, false));

        // Setup JXMapViewer
        JXMapViewer mapViewer = new JXMapViewer();
        mapViewer.setTileFactory(tileFactory);

        // Set the focus
        mapViewer.setZoom(13);

        JFrame frame = new JFrame("K-Means Clustering");

        if(gui){
            GeoPosition uslar = new GeoPosition(51.268805, 10.271973);
            mapViewer.setAddressLocation(uslar);

            // Add interactions
            MouseInputListener mia = new PanMouseInputListener(mapViewer);
            mapViewer.addMouseListener(mia);
            mapViewer.addMouseMotionListener(mia);
            mapViewer.addMouseListener(new CenterMapListener(mapViewer));
            mapViewer.addMouseWheelListener(new ZoomMouseWheelListenerCenter(mapViewer));
            mapViewer.addKeyListener(new PanKeyListener(mapViewer));

            frame.setSize(800, 600);
            frame.getContentPane().add(mapViewer);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        }


        ReadWriteLock lock = new ReentrantReadWriteLock();


        // task for a single K-means cycle
        TimerTask kmeansUpdate = new TimerTask() {
            private final long startTime = System.nanoTime();
            private int rounds = 0;

            @Override
            public void run() {
                try {
                    // multithreading safety, makes sure UI can't update in the middle of a K-means run
                    //  this doesn't affect programs ran in sequential and distributed modes
                    lock.writeLock().lock();
                    kMeans.update();

                    // synchronized makes sure the plants resource is only used here
                    //  not having this can cause problems during multithreading
                    synchronized (plants) {
                        plantGenerator.updatePlants(kMeans.getCentroids());
                        plants.clear();
                        plants.addAll(plantGenerator.getPlants());
                    }

                    synchronized (sites) {
                        sites.clear();

                        for (int i = 0; i < clusterCount; i++) {
                            for (Point point : kMeans.getClusters().get(i)) {
                                sites.add(new AccumulationSite(plantGenerator.getPlants().get(i).getColor(), new GeoPosition(point.getLa(), point.getLo())));
                            }
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Error in worker thread: " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    rounds++;
                    lock.writeLock().unlock();
                }

                // when we reach the designated number of cycles
                //  print out how much time computation took
                //  tell MPI we're done with distributed workloads
                //  cancel the current task
                if (rounds == cycles) {
                    System.out.println("--------------------------------");
                    System.out.println("| Computation done: " + (System.nanoTime() - startTime) / 1000000 + " ms");
                    System.out.println("--------------------------------");
                    MPI.Finalize();
                    this.cancel();
                }
            }
        };

        // initial update
        //  needs to run at least once before UI starts
        kmeansUpdate.run();

        // worker thread, updates every 10ms
        kmeansExecutor.scheduleWithFixedDelay(kmeansUpdate, 0, 10, TimeUnit.MILLISECONDS);

        // UI thread, updates every (1000 / 60) ms, i.e. 60 FPS
        kmeansExecutor.scheduleWithFixedDelay(() -> {
            try {
                lock.readLock().lock();

                mapViewer.removeAll();

                // create copies of plants and sites to ensure they don't get modified during render
                Set<ProcessingPlant> localPlants = new HashSet<>(plants);
                Set<AccumulationSite> localSites = new HashSet<>(sites);

                for (ProcessingPlant p : localPlants) {
                    mapViewer.add(p.getPlant());
                }

                for (AccumulationSite s : localSites) {
                    mapViewer.add(s.getAccumSite());
                }

                WaypointPainter<AccumulationSite> accPainter = new AccumulationSitePainter();
                accPainter.setWaypoints(localSites);

                WaypointPainter<ProcessingPlant> plantPainter = new PlantPainter();
                plantPainter.setWaypoints(localPlants);

                // combine the painters into a compound painter
                List<Painter<JXMapViewer>> painters = new ArrayList<>();
                painters.add(accPainter);
                painters.add(plantPainter);

                CompoundPainter<JXMapViewer> painter = new CompoundPainter<>(painters);
                mapViewer.setOverlayPainter(painter);


                frame.setVisible(gui);

            } catch (Exception e) {
                System.out.println("Error in UI thread: " + e.getMessage());
                e.printStackTrace();
            } finally {
                lock.readLock().unlock();
            }
        }, 1000 / 60, 1000 / 60, TimeUnit.MILLISECONDS);

    }

    // a very primitive method for parsing command-line arguments
    private static Map<String, Integer> parseArgs(String[] args) {
        Map<String, Integer> argMap = new HashMap<>();

        for (int i = 0; i < args.length; i++) {
            String arg = args[i].toLowerCase();
            if (!arg.startsWith("--") || arg.contains(" ") || arg.length() == 2)
                continue;

            String name = arg.substring(2).trim();
            int value = -1;
            if (i != args.length - 1 && !args[i +1].trim().startsWith("--")) {
                try {
                    value = Integer.parseInt(args[i + 1]);
                } catch (NumberFormatException e) { /* ignored */}
            }

            argMap.put(name, value);
        }

        return argMap;
    }
}