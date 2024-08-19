import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.jxmapviewer.viewer.GeoPosition;

public class PlantGenerator {

    private final Random random;

    private final ArrayList<Point> points;

    ArrayList<Point> pointsOfPlants = new ArrayList<>();

    List<ProcessingPlant> plants = new ArrayList<>();
    private final int k;

    public PlantGenerator(Random random, ArrayList<Point> points, int k) {
        this.random = random;
        this.points = points;
        this.k = k;
    }

    public List<ProcessingPlant> generatePlants() {
        // randomly select k distinct points as initial plant positions
        List<Point> shuffledPoints = new ArrayList<>(points);
        Collections.shuffle(shuffledPoints, random);
        for (int i = 0; i < k; i++) {
            Point p = shuffledPoints.get(i);
            pointsOfPlants.add(p);
        }
        for (Point p : pointsOfPlants) {
            GeoPosition coord = new GeoPosition(p.getLa(), p.getLo());
            float r = random.nextFloat();
            float g = random.nextFloat();
            float b = random.nextFloat();
            plants.add(new ProcessingPlant(new Color(r,g,b), coord));
        }
        return plants;
    }

    public ArrayList<Color> getColors(){
        ArrayList<Color> colors = new ArrayList<>();
        for (ProcessingPlant p : plants){
            colors.add(p.getColor());
        }
        return colors;
    }

    public void updatePlants(List<Point> points){
        List<ProcessingPlant> newPlants = new ArrayList<>();
        for (int i = 0; i < k; i++) {
            Point p = points.get(i);
            GeoPosition coord = new GeoPosition(p.getLa(), p.getLo());
            newPlants.add(new ProcessingPlant(getColors().get(i), coord));
        }
        plants = newPlants;
    }

    public List<ProcessingPlant> getPlants() {
        return plants;
    }
    public ArrayList<Point> getPointsOfPlants() {
        return pointsOfPlants;
    }
}



