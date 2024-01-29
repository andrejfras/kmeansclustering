import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.jxmapviewer.viewer.GeoPosition;

public class PlantGenerator {

    private final ArrayList<Point> points;

    ArrayList<Point> pointsOfPlants = new ArrayList<>();

    List<ProcessingPlant> plants = new ArrayList<>();
    private final int k;

    public PlantGenerator(ArrayList<Point> points, int k) {
        this.points = points;
        this.k = k;
    }

    public List<ProcessingPlant> generatePlants() {
        Random random = new Random();

        // Randomly select k distinct points as initial plant positions
        List<Point> shuffledPoints = new ArrayList<>(points);
        Collections.shuffle(shuffledPoints);
        Random rand = new Random();
        for (int i = 0; i < k; i++) {
            Point p = shuffledPoints.get(i);
            pointsOfPlants.add(p);
        }
        for (Point p : pointsOfPlants) {
            GeoPosition coord = new GeoPosition(p.getLa(), p.getLo());
            float r = rand.nextFloat();
            float g = rand.nextFloat();
            float b = rand.nextFloat();
            plants.add(new ProcessingPlant(new Color(r,g,b), coord));
        }
        return plants;
    }

    public void clearPlants(){
        for (ProcessingPlant plant : plants) {
            plants.remove(plant);
        }
    }

    public ArrayList<Color> getColors(){
        ArrayList<Color> colors = new ArrayList<>();
        for (ProcessingPlant p : plants){
            colors.add(p.getColor());
        }
        return colors;
    }

    public List<ProcessingPlant> updatePlants(ArrayList<Point> points){
        List<ProcessingPlant> newPlants = new ArrayList<>();
        for (int i = 0; i < k; i++) {
            Point p = points.get(i);
            GeoPosition coord = new GeoPosition(p.getLa(), p.getLo());
        //    System.out.println(p.getLa() + " " + p.getLo() );
          //  System.out.println(getColors().get(i));
            newPlants.add(new ProcessingPlant(getColors().get(i), coord));
        }
        plants = newPlants;
       // System.out.println(plants.size());
        return plants;
    }



    public List<ProcessingPlant> getPlants() {

        //System.out.println(plants.size());
        return plants;
    }
    public ArrayList<Point> getPointsOfPlants() {
        return pointsOfPlants;
    }
}



