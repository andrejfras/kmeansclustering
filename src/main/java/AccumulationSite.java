import org.jxmapviewer.viewer.DefaultWaypoint;
import org.jxmapviewer.viewer.GeoPosition;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

public class AccumulationSite extends DefaultWaypoint {

    private final JPanel accumSite;
    private GeoPosition coord;


    //accomulation site object
    public AccumulationSite(Color color, GeoPosition coord) {
        super(coord);
        this.coord = coord;
        accumSite = new JPanel();
        accumSite.setOpaque(false);
        accumSite.setBorder(new LineBorder(color,20, true));
        accumSite.setSize(5,5);
        accumSite.setPreferredSize(new Dimension(5,5));
        accumSite.setVisible(true);
    }

    public JPanel getAccumSite() {
        return accumSite;
    }

    public void setSize(int width, int height) {
        accumSite.setSize(width, height);
        accumSite.setPreferredSize(new Dimension(width, height));
    }


    public double distTo(ProcessingPlant plant) {
        double latDiff = coord.getLatitude() - plant.getPosition().getLatitude();
        double lonDiff = coord.getLongitude() - plant.getPosition().getLongitude();
        return Math.sqrt(Math.pow(latDiff,2) + Math.pow(lonDiff,2));
    }


      /*
    public AccumulationSite(GeoPosition coord) {
        super(coord);
        this.coord = coord;
        accumSite = new JPanel();
        accumSite.setOpaque(false);
        accumSite.setBorder(new LineBorder(Color.black,5, true));
        accumSite.setSize(5,5);
        accumSite.setPreferredSize(new Dimension(5,5));
        accumSite.setVisible(true);
    }


     */
}
