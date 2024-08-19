import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

// parallel/multithreaded K-means runner
public class KMeansMultithreaded implements IKMeans {

    // (CPUS - 2) because 2 are used as UI/worker threads
    private static final int THREADS = Math.max(1, Runtime.getRuntime().availableProcessors() - 2);

    // one executor for all instances, since it's limited to CPU cores
    //  either way, in the context of the application, there should only be once instance
    private static final Executor executor = Executors.newFixedThreadPool(THREADS);

    private final int k;
    private final List<Point> points;
    private final List<Point> centroids;
    private final List<List<Point>> clusters;

    public KMeansMultithreaded(int k, List<Point> sites, ArrayList<Point> plants) {
        this.k = k;
        this.points = sites;
        this.centroids = plants;
        this.clusters = new ArrayList<>();
    }

    private void InitializeClusters(){
        clusters.clear();
        for(int i = 0; i < k; i++){
            clusters.add(Collections.synchronizedList(new ArrayList<>()));
            clusters.get(i).add(centroids.get(i));
        }
    }

    private void assignPointsToClusters() {
        InitializeClusters();

        // send parts of workload to threads
        int workers = Math.min(points.size(), THREADS);
        final CountDownLatch latch = new CountDownLatch(workers);
        for (List<Point> cluster : partition(points, workers)) {
            executor.execute(() -> {
                for (Point dataPoint : cluster) {
                    int closestCentroidIndex = getClosestCentroidIndex(dataPoint);
                    clusters.get(closestCentroidIndex).add(dataPoint);
                }
                latch.countDown();
            });
        }

        try {
            // wait for all threads to finish working
            latch.await();
        } catch (InterruptedException e) {/* ignored */}
    }

    private int getClosestCentroidIndex(Point dataPoint) {
        int closestCentroidIndex = 0;
        double minDistance = Double.MAX_VALUE;
        for (int i = 0; i < k; i++) {
            double distance = euclideanDistance(dataPoint, centroids.get(i));
            if (distance < minDistance) {
                closestCentroidIndex = i;
                minDistance = distance;
            }
        }
        return closestCentroidIndex;
    }

    private double euclideanDistance(Point a, Point b) {
        double dx = a.getLa() - b.getLa();
        double dy = a.getLo() - b.getLo();
        return Math.sqrt(dx * dx + dy * dy);
    }

    void updateCentroids() {
        // send parts of workload to threads
        int workers = Math.min(k, THREADS);
        final CountDownLatch latch = new CountDownLatch(workers);

        List<Integer> indices = IntStream.range(0, k).boxed().collect(Collectors.toList());
        for (List<Integer> ii : partition(indices, workers)) {
            executor.execute(() -> {
                for (int i : ii) {
                    Point centroid = new Point(0, 0);
                    if (clusters.get(i).isEmpty()) {
                        continue;
                    }

                    for (Point dataPoint : clusters.get(i)) {
                        centroid.setLa(centroid.getLa() + dataPoint.getLa());
                        centroid.setLo(centroid.getLo() + dataPoint.getLo());
                    }
                    centroid.setLa(centroid.getLa() / clusters.get(i).size());
                    centroid.setLo(centroid.getLo() / clusters.get(i).size());
                    centroids.set(i, centroid);
                }
                latch.countDown();
            });
        }

        try {
            // wait for all threads to finish working
            latch.await();
        } catch (InterruptedException e) {/* ignored */}
    }

    public void update(){
        assignPointsToClusters();
        updateCentroids();
    }

    public List<Point> getCentroids(){
        return centroids;
    }

    public  List<List<Point>> getClusters(){
        return clusters;
    }

    // split some data into chunks of size
    //  we use this when distributing workload to threads
    //  each thread gets it's own chunk of data to work on
    private static <T> List<List<T>> partition(List<T> list, int size) {
        List<List<T>> result = new ArrayList<>();
        int perCluster = (int) Math.ceil((double) list.size() / size);
        for (int i = 0; i < size; i++) {
            int start = Math.min(i * perCluster, list.size());
            int end = Math.min(start + perCluster, list.size());
            result.add(list.subList(start, end));
        }
        return result;
    }

}
