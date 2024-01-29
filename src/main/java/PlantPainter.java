import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.WaypointPainter;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;

public class PlantPainter extends WaypointPainter<ProcessingPlant> {

    @Override
    protected void doPaint(Graphics2D g, JXMapViewer jxMapViewer, int width, int height) {
        for (ProcessingPlant processPlant : getWaypoints()) {
            Point2D point = jxMapViewer.getTileFactory().geoToPixel(
                    processPlant.getPosition(), jxMapViewer.getZoom());
            Rectangle rectangle = jxMapViewer.getViewportBounds();
            int buttonX = (int)(point.getX() - rectangle.getX());
            int buttonY = (int)(point.getY() - rectangle.getY());
            JPanel panel = processPlant.getPlant();
            panel.setLocation(buttonX - panel.getWidth() / 2, buttonY - panel.getHeight() / 2);
        }
    }
}
