import java.util.ArrayList;
import java.util.List;

// base K-means implementation, i.e. sequential
public class KMeans implements IKMeans {

    private final int clusterCount;
    private final List<Point> points;
    private final List<Point> centroids;
    private final List<List<Point>> clusters;

    
    public KMeans(int clusterCount, List<Point> sites, ArrayList<Point> plants) {
        this.clusterCount = clusterCount;
        this.points = sites;
        this.centroids = plants;
        this.clusters = new ArrayList<>();
    }

    private void InitializeClusters(){
        clusters.clear();
        for(int i = 0; i < clusterCount; i++){
            clusters.add(new ArrayList<>());
            clusters.get(i).add(centroids.get(i));
        }
    }

    private void assignPointsToClusters() {
        InitializeClusters();
        for (Point dataPoint : points) {
            int closestCentroidIndex = getClosestCentroidIndex(dataPoint);
            clusters.get(closestCentroidIndex).add(dataPoint);
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


}


