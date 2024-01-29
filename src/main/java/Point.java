import java.awt.*;

public class Point {
    private String name;
    private double capacity;
    private double la;
    private double lo;


    private int cluster;

    public Point(String name, double capacity, double la, double lo, int cluster) {
        this.name = name;
        this.capacity = capacity;
        this.la = la;
        this.lo = lo;
        this.cluster = cluster;
    }

    public String getName() {
        return name;
    }

    public double getCapacity() {
        return capacity;
    }

    public double getLa() {
        return la;
    }

    public double getLo() {
        return lo;
    }

    public int getCluster() {
        return cluster;
    }

    public void setCluster(int cluster) {
        this.cluster = cluster;
    }

    public double distanceTo(Point other) {
        double dx = this.la - other.getLa();
        double dy = this.lo - other.getLo();
        return Math.sqrt(dx * dx + dy * dy);
    }

    public void setLa(double la) {
        this.la = la;
    }

    public void setLo(double lo) {
        this.lo = lo;
    }
}

