import org.jxmapviewer.viewer.DefaultWaypoint;
import org.jxmapviewer.viewer.GeoPosition;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.Objects;

public class AccumulationSite extends DefaultWaypoint {

    private final JPanel accumSite;
    private final GeoPosition coord;


    // accumulation site object
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

    // the following 2 methods will tell all collections (sets, maps, lists, etc.) that if there are 2
    //  accumulations sites, they are the same if only this.coord is the same

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccumulationSite that = (AccumulationSite) o;
        return Objects.equals(coord, that.coord);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(coord);
    }
}
