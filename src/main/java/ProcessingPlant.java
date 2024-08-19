import org.jxmapviewer.viewer.DefaultWaypoint;
import org.jxmapviewer.viewer.GeoPosition;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.Objects;

public class ProcessingPlant extends DefaultWaypoint {

    private final JPanel plant;
    private final GeoPosition coord;

    private final Color color;

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
    }

    public Color getColor() {
        return color;
    }

    public JPanel getPlant() {
        return plant;
    }

    @Override
    public void setPosition(GeoPosition coordinate) {
        super.setPosition(coordinate);
    }

    public double getLa() {
        return super.getPosition().getLatitude();
    }

    public double getLo() {
        return super.getPosition().getLongitude();
    }

    // the following 2 methods will tell all collections (sets, maps, lists, etc.) that if there are 2
    //  accumulations sites, they are the same if only this.coord is the same
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProcessingPlant that = (ProcessingPlant) o;
        return Objects.equals(coord, that.coord);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(coord);
    }
}
