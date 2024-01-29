import org.jxmapviewer.viewer.DefaultWaypoint;
import org.jxmapviewer.viewer.GeoPosition;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.ArrayList;
public class ProcessingPlant extends DefaultWaypoint {

    private final JPanel plant;
    private ArrayList<Point> members;
    private GeoPosition coord;

    private Color color;

    public ProcessingPlant(Color color, GeoPosition coord) {
        super(coord);
        this.color = color;
        this.coord = coord;
        plant = new JPanel();
        plant.setOpaque(false);
        plant.setBorder(new LineBorder(color, 20, true));
        plant.setSize(30, 30);
        plant.setPreferredSize(new Dimension(30, 30));
        plant.setVisible(true);
        members = new ArrayList<>();
    }

    public Color getColor() {
        return color;
    }

    public JPanel getPlant() {
        return plant;
    }

    public void setSize(int width, int height) {
        plant.setSize(width, height);
        plant.setPreferredSize(new Dimension(width, height));
    }

    public void addMember(Point p) {
        members.add(p);
    }

    public void clearMembers() {
        members.clear();
    }

    public void updatePosition() {
        if (members.size() == 0) {
            return;
        }
        double sumLat = 0;
        double sumLon = 0;
        for (Point p : members) {
            sumLat += p.getLa();
            sumLon += p.getLo();
        }
        double newLat = sumLat / members.size();
        double newLon = sumLon / members.size();
        coord = new GeoPosition(newLat, newLon);
        setPosition(coord);
    }

    public double distTo(Point p) {
        double latDiff = coord.getLatitude() - p.getLa();
        double lonDiff = coord.getLongitude() - p.getLo();
        return Math.sqrt(Math.pow(latDiff, 2) + Math.pow(lonDiff, 2));
    }

    @Override
    public void setPosition(GeoPosition coordinate) {
        super.setPosition(coordinate);
    }

    @Override
    public GeoPosition getPosition() {
        return super.getPosition();
    }

    public double getLa() {
        return super.getPosition().getLatitude();
    }

    public double getLo() {
        return super.getPosition().getLongitude();
    }
}
