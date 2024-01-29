import java.awt.*;
import java.util.ArrayList;

public class KMeans {

    private int k;
    private ArrayList<Point> points;
    private ArrayList<Point> centroids;
    private ArrayList<ArrayList<Point>> clusters;

    
    public KMeans(int k, ArrayList<Point> sites, ArrayList<Point> plants) {
        this.k = k;
        this.points = sites;
        this.centroids = plants;
        this.clusters = new ArrayList<ArrayList<Point>>();
    }

    private void InitializeClusters(){
        for(int i = 0; i < k; i++){
            clusters.add(new ArrayList<Point>());
            clusters.get(i).add(centroids.get(i));
        }
    }

    private void assignPointsToClusters() {
        clusters.clear();
        InitializeClusters();
        for (Point dataPoint : points) {
            int closestCentroidIndex = getClosestCentroidIndex(dataPoint);
            clusters.get(closestCentroidIndex).add(dataPoint);
        }
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
        for (int i = 0; i < k; i++) {
            Point centroid = new Point("", 0, 0, 0, i);
            if (clusters.get(i).size() == 0) {
                continue;
            }
            for (Point dataPoint : clusters.get(i)) {
                dataPoint.setCluster(i);
                centroid.setLa(centroid.getLa() + dataPoint.getLa());
                centroid.setLo(centroid.getLo() + dataPoint.getLo());
            }
            centroid.setLa(centroid.getLa() / clusters.get(i).size());
            centroid.setLo(centroid.getLo() / clusters.get(i).size());
            centroids.set(i, centroid);
        }
    }

    void setClusterColor(){
        for(Point point : centroids){
            for(int i= 0; i<k; i++){
                for(Point p: clusters.get(i)){
                    p.setCluster(i);
                }
            }
        }
    }

    void updatePointColor() {
        for (int i = 0; i < k; i++) {
            for (Point dataPoint : clusters.get(i)) {
                dataPoint.setCluster(i);
            }
        }
    }

    public int update(){

        assignPointsToClusters();
        updateCentroids();
     /*   System.out.println("cent1 "+ centroids.get(0).getLa() + " " +centroids.get(0).getLo());
        System.out.println("cent2 "+ centroids.get(1).getLa() + " " +centroids.get(1).getLo());
        System.out.println("cent3 "+ centroids.get(2).getLa() + " " +centroids.get(2).getLo());
        System.out.println("cent4 "+ centroids.get(3).getLa() + " " +centroids.get(3).getLo());
        System.out.println("cent5 "+ centroids.get(4).getLa() + " " +centroids.get(4).getLo());

      */


        return clusters.size();
    }

    public ArrayList<Point> getCentroids(){
        return centroids;
    }

    public  ArrayList<ArrayList<Point>> getClusters(){
        return clusters;
    }




    /*

    public void cluster() {
        boolean converged = false;
        while (!converged) {
            assignpointsToClusters();
            updateCentroids();
            converged = hasConverged();
        }
    }
    private void initializeCentroids() {
        Random random = new Random();
        for (int i = 0; i < k; i++) {
            int index = random.nextInt(points.size());
            centroids.add(points.get(index));
        }
    }

    private void assignpointsToClusters() {
        clusters.clear();
        for (int i = 0; i < k; i++) {
            clusters.add(new ArrayList<Point>());
        }
        for (Point dataPoint : points) {
            int closestCentroidIndex = getClosestCentroidIndex(dataPoint);
            clusters.get(closestCentroidIndex).add(dataPoint);
        }
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

    private void updateCentroids() {
        for (int i = 0; i < k; i++) {
            Point centroid = new Point("", 0, 0, 0, i);
            if (clusters.get(i).size() == 0) {
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
    }

    private boolean hasConverged() {
        for (int i = 0; i < k; i++) {
            if (!comparePoints(centroids.get(i), points.get(i))) {
                return false;
            }
        }
        return true;
    }

    private boolean comparePoints(Point a, Point b) {
        return a.getLa() == b.getLa() && a.getLo() == b.getLo();
    }

    public ArrayList<ArrayList<Point>> getClusters() {
        return clusters;
    }

    public ArrayList<Point> update() {
        // Reset cluster assignments and centroid sums
        for (ArrayList<Point>  cluster : clusters) {
            cluster.clear();
        }

        // Assign data points to the closest cluster and update centroid sums
        for (Point point : points) {
            ArrayList<Point>  closestCluster = null;
            double minDistance = Double.MAX_VALUE;

            for (ArrayList<Point> cluster : clusters) {
                int closestCentroidIndex = getClosestCentroidIndex(point);
                double distance = point.distanceTo(cluster.get(closestCentroidIndex));

                if (distance < minDistance) {
                    closestCluster = cluster;
                    minDistance = distance;
                }
            }

            if(closestCluster != null) {
                closestCluster.add(point);
            }
        }

        // Update centroids
        updateCentroids();

        return centroids;
    }

    public ArrayList<Point> getCentroids(){
        return centroids;
    }

    public void plusEna(){
        for(Point p : centroids){
            p.setLa(p.getLa() + 1);
        }
    }


     */

}


