import java.util.ArrayList;

public class Cluster {
    private int id;
    private ArrayList<Point> points;
    private Point centroid;

    public Cluster(int id, Point centroid) {
        this.id = id;
        this.centroid = centroid;
        this.points = new ArrayList<Point>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ArrayList<Point> getPoints() {
        return points;
    }

    public void setPoints(ArrayList<Point> points) {
        this.points = points;
    }

    public Point getCentroid() {
        return centroid;
    }

    public void setCentroid(Point centroid) {
        this.centroid = centroid;
    }

    public void addPoint(Point point) {
        points.add(point);
    }

    public void clear() {
        points.clear();
    }

    public void calculateCentroid() {
        double sumLat = 0.0;
        double sumLong = 0.0;
        for (Point point : points) {
            sumLat += point.getLa();
            sumLong += point.getLo();
        }
        double avgLat = sumLat / points.size();
        double avgLong = sumLong / points.size();
        centroid.setLa(avgLat);
        centroid.setLo(avgLong);
    }
}
