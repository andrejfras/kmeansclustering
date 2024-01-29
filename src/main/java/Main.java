import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.File;
import java.util.*;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.event.MouseInputListener;
import javax.swing.plaf.basic.BasicDesktopIconUI;

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
    public static void main(String[] args) {

        //number of clusters
        int k = 11;
        //number od points
        int n = 2000;
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

        GeoPosition uslar = new GeoPosition(51.268805, 10.271973);
        mapViewer.setAddressLocation(uslar);


        // Add interactions
        MouseInputListener mia = new PanMouseInputListener(mapViewer);
        mapViewer.addMouseListener(mia);
        mapViewer.addMouseMotionListener(mia);
        mapViewer.addMouseListener(new CenterMapListener(mapViewer));
        mapViewer.addMouseWheelListener(new ZoomMouseWheelListenerCenter(mapViewer));
        mapViewer.addKeyListener(new PanKeyListener(mapViewer));

        // Create points from the geo-positions
        Set<AccumulationSite> points = new HashSet<AccumulationSite>();
        ArrayList<Point> points2 = new ArrayList<Point>();
        Set<ProcessingPlant> plants = new HashSet<ProcessingPlant>();

        PointGenerator pointGenerator = new PointGenerator(n);
        for (Point point : pointGenerator.getPoints()) {
            points.add(new AccumulationSite(Color.RED, new GeoPosition(point.getLa(), point.getLo())));
            points2.add(point);
        }
        System.out.println(points2.size());

        PlantGenerator plantGenerator = new PlantGenerator(pointGenerator.getPoints(), k);
        plantGenerator.generatePlants();

        /*
        for (ProcessingPlant plant : plantGenerator.getPlants()) {
            Random rand = new Random();
            float r = rand.nextFloat();
            float g = rand.nextFloat();
            float b = rand.nextFloat();
            plants.add(new ProcessingPlant(new Color(r, g, b), new GeoPosition(plant.getLa(), plant.getLo())));
            System.out.println(plant.getLa() + " " + plant.getLo());
        }


         */

        // Set the overlay painter

/*
        WaypointPainter<AccumulationSite> accPainter = new SwingWaypointOverlayPainter();
        accPainter.setWaypoints(points);

        WaypointPainter<ProcessingPlant> plantPainter = new PlantPainter();
        plantPainter.setWaypoints(plants);

        // combine the painters into a compound painter
        List<Painter<JXMapViewer>> painters = new ArrayList<Painter<JXMapViewer>>();
        painters.add(plantPainter);
        painters.add(accPainter);


        CompoundPainter<JXMapViewer> painter = new CompoundPainter<JXMapViewer>(painters);
        mapViewer.setOverlayPainter(painter);

 */




        // Start KMeans
        KMeans kmeans = new KMeans(k, points2, plantGenerator.getPointsOfPlants());

        // Add the JButtons to the map viewer
    /*    for (ProcessingPlant p : plants) {
            mapViewer.add(p.getPlant());
        }


     */

        for (AccumulationSite w : points) {
            mapViewer.add(w.getAccumSite());
        }

        plantGenerator.updatePlants(kmeans.getCentroids());

        for (ProcessingPlant plant : plantGenerator.getPlants()) {
            System.out.println(plant.getLa() + " " + plant.getLo());
        }

/*
        kmeans.check();
        kmeans.updateCentroids();
        plantGenerator.updatePlants(kmeans.getCentroids());


 */

        // Display the viewer in a JFrame

        /*
        JFrame frame = new JFrame("K-Means Clustering");
        frame.getContentPane().add(mapViewer);
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

         */



      //  System.out.println(kmeans.check());

        JFrame frame = new JFrame("K-Means Clustering");
        frame.setSize(800, 600);
        frame.getContentPane().add(mapViewer);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Set<ProcessingPlant> plantCheck = new HashSet<ProcessingPlant>();



        while (true) {

            mapViewer.removeAll();


            for (ProcessingPlant p : plants) {
                mapViewer.remove(p.getPlant());
            }

            for (AccumulationSite a : points) {
                mapViewer.remove(a.getAccumSite());
            }
           // plantCheck = plants;
            for (ProcessingPlant plant : plantGenerator.getPlants()) {
                plants.remove(plant);
            }



     /*       for (Point point : pointGenerator.getPoints()) {
                points.remove(point);
            }

      */

            plants.clear();

            points.clear();

          //  System.out.println(plants.size());
            kmeans.update();
            plantGenerator.updatePlants(kmeans.getCentroids());

         //   System.out.println(plants.size());

           // System.out.println(plant.getLa() + " " + plant.getLo());
            plants.addAll(plantGenerator.getPlants());    //lahko uporabimo array list


           // System.out.println(plants.size());

            //System.out.println(points.size());

            for(Point p : kmeans.getCentroids()){
                points.remove(p);
            }

            System.out.println(points.size());

            for(int i = 0; i< k ; i++) {
                for (Point a : kmeans.getClusters().get(i)){
                    for(Point p : kmeans.getCentroids()){
                        if(a==p) {
                            break;
                        }
                    }
                    points.add(new AccumulationSite(plantGenerator.getPlants().get(i).getColor(), new GeoPosition(a.getLa(), a.getLo())));
                }
            }

            System.out.println(points.size());

            for (ProcessingPlant p : plants) {
                mapViewer.add(p.getPlant());
            }

            for (AccumulationSite s: points){
                mapViewer.add(s.getAccumSite());
            }

            WaypointPainter<AccumulationSite> accPainter = new SwingWaypointOverlayPainter();
            accPainter.setWaypoints(points);

            System.out.println(plants.size());

            WaypointPainter<ProcessingPlant> plantPainter = new PlantPainter();
            plantPainter.setWaypoints(plants);

            // combine the painters into a compound painter
            List<Painter<JXMapViewer>> painters = new ArrayList<Painter<JXMapViewer>>();
            painters.add(accPainter);
            painters.add(plantPainter);

            CompoundPainter<JXMapViewer> painter = new CompoundPainter<JXMapViewer>(painters);
            mapViewer.setOverlayPainter(painter);

         //   frame.repaint();

            frame.setVisible(true);




// Add the new plants to the mapViewer

        //    plantPainter.

            plants.clear();

            // Wait for 100 milliseconds
            try {
                Thread.sleep(330);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
/*

        52.3292 12.3583
        47.5667 8.0333
        47.9213 8.4991
        50.2333 6.25
        49.1406 8.0186

        50.20805593466418 7.518839310344838
        48.59599196230591 9.572310476718402
        52.65451966173369 12.822709408033832
        53.435097181146034 9.65928923290206
        50.19488151735278 11.537544915254227

        51.2547303482587 11.215505201266375
        53.5880998977505 9.53965051124748
        48.68625675012263 10.95641782032401
        52.76144252948887 13.115517889908281
        49.89556437704435 7.715317573595

 */