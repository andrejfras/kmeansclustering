import mpi.Intracomm;
import mpi.MPI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// distributed K-means runner, sends tasks to workers and waits for response
public class KMeansDistributed implements IKMeans {

    private static final Intracomm comm = MPI.COMM_WORLD;

    private final int clusterCount;
    private final List<Point> points;
    private final List<Point> centroids;
    private final List<List<Point>> clusters;

    private final int size;

    public KMeansDistributed(int clusterCount, List<Point> sites, ArrayList<Point> plants, int size) {
        this.clusterCount = clusterCount;
        this.points = sites;
        this.centroids = plants;
        this.clusters = new ArrayList<>();

        this.size = size;

        // no point in distributed if only one
        assert size >= 2;
    }

    private void InitializeClusters(){
        clusters.clear();
        for(int i = 0; i < clusterCount; i++){
            clusters.add(Collections.synchronizedList(new ArrayList<>()));
            clusters.get(i).add(centroids.get(i));
        }
    }

    private void assignPointsToClusters() {
        InitializeClusters();

        List<List<Point>> partitioned = partition(points, size);

        // rank 0 work
        List<Point> myCluster = partitioned.get(0);
        for (Point dataPoint : myCluster) {
            int closestCentroidIndex = getClosestCentroidIndex(dataPoint);
            clusters.get(closestCentroidIndex).add(dataPoint);
        }

        List<List<Point>> theirClusters = partitioned.subList(1, partitioned.size());

        int curr = 1;
        for (List<Point> cluster : theirClusters) {
            double[] data = new double[cluster.size() * 2];

            for (int i = 0; i < cluster.size(); i++) {
                data[i * 2] = cluster.get(i).getLo();
                data[i * 2 + 1] = cluster.get(i).getLa();
            }

            comm.Send(new int[] {data.length}, 0, 1, MPI.INT, curr, 0);
            comm.Send(data, 0, data.length, MPI.DOUBLE, curr, 1);
            curr++;
        }

        curr = 1;
        for (List<Point> cluster : theirClusters) {
            int[] data = new int[cluster.size()];

            comm.Recv(data, 0, data.length, MPI.INT, curr, 3);

            for (int i = 0; i < data.length; i++) {
                clusters.get(data[i]).add(cluster.get(i));
            }
            curr++;
        }
    }

    private int getClosestCentroidIndex(Point dataPoint) {
        int closestCentroidIndex = 0;
        double minDistance = Double.MAX_VALUE;
        for (int i = 0; i < clusterCount; i++) {
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
        // there's not really a good way to distribute this
        //  so we just send each worker an update of centroids

        double[] data = new double[clusterCount * 2];

        for (int i = 0; i < clusterCount; i++) {
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
            data[i * 2] = centroid.getLo();
            data[i * 2 + 1] = centroid.getLa();
        }

        for (int i = 1; i < size; i++) {
            comm.Send(data, 0, data.length, MPI.DOUBLE, i, 4);
        }
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
