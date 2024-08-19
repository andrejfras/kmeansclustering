import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.WaypointPainter;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;

/**
 * "Paints" the Swing waypoints. In fact, just takes care of correct positioning of the representing button.
 *
 * @author Daniel Stahr
 */
public class AccumulationSitePainter extends WaypointPainter<AccumulationSite> {

    @Override
    protected void doPaint(Graphics2D g, JXMapViewer jxMapViewer, int width, int height) {
        for (AccumulationSite accumSite : getWaypoints()) {
            Point2D point = jxMapViewer.getTileFactory().geoToPixel(
                    accumSite.getPosition(), jxMapViewer.getZoom());
            Rectangle rectangle = jxMapViewer.getViewportBounds();
            int buttonX = (int)(point.getX() - rectangle.getX());
            int buttonY = (int)(point.getY() - rectangle.getY());
            JPanel panel = accumSite.getAccumSite();
            panel.setLocation(buttonX - panel.getWidth() / 2, buttonY - panel.getHeight() / 2);
        }
    }
}