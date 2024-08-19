import mpi.Intracomm;
import mpi.MPI;

import java.util.ArrayList;
import java.util.List;

// distributed K-means workers, these run only in the processes that are not the main one (UI process)
public class KMeansDistributedWorker {

    private static final Intracomm comm = MPI.COMM_WORLD;

    private final int clusterCount;
    private final List<Point> centroids;

    public KMeansDistributedWorker(int clusterCount, ArrayList<Point> plants) {
        this.clusterCount = clusterCount;
        this.centroids = plants;
    }

    public void run() {
        // stop listening for workload when done
        //  MPI.Finalize() called in main will toggle the value of this
        if (!MPI.Initialized())
            return;

        // begin the closest centroid index computation
        int[] count = new int[1];
        comm.Recv(count, 0, 1, MPI.INT, 0, 0);

        double[] data = new double[count[0]];
        comm.Recv(data, 0, data.length, MPI.DOUBLE, 0, 1);

        int[] back = new int[data.length / 2];
        for (int i = 0; i < back.length; i++) {
            double lo = data[i * 2];
            double la = data[i * 2 + 1];

            int closestIndex = getClosestCentroidIndex(lo, la);
            back[i] = closestIndex;
        }

        comm.Send(back, 0, back.length, MPI.INT, 0, 3);
        // end the closest centroid index computation

        // begin cluster centroid sync
        double[] updatedCentroids = new double[clusterCount * 2];
        comm.Recv(updatedCentroids, 0, updatedCentroids.length, MPI.DOUBLE, 0, 4);

        for (int i = 0; i < clusterCount; i++) {
            double lo = updatedCentroids[i * 2];
            double la = updatedCentroids[i * 2 + 1];
            centroids.set(i, new Point(la, lo));
        }
        // end cluster centroid sync

        // run again, i.e. wait for new tasks
        run();
    }

    private int getClosestCentroidIndex(double aLo, double aLa) {
        int closestCentroidIndex = 0;
        double minDistance = Double.MAX_VALUE;
        for (int i = 0; i < clusterCount; i++) {
            double distance = euclideanDistance(aLo, aLa, centroids.get(i));
            if (distance < minDistance) {
                closestCentroidIndex = i;
                minDistance = distance;
            }
        }
        return closestCentroidIndex;
    }

    private double euclideanDistance(double aLo, double aLa, Point b) {
        double dx = aLa - b.getLa();
        double dy = aLo - b.getLo();
        return Math.sqrt(dx * dx + dy * dy);
    }

}
