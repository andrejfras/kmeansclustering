import com.google.gson.Gson;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

public class PointGenerator {

    private final ArrayList<Point> points = new ArrayList<>();

    public PointGenerator(Random random, int clusterCount) {
        String json = null;
        try {
            byte[] jsonData = Files.readAllBytes(Paths.get("src/main/resources/germany.json"));
            json = new String(jsonData);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Gson gson = new Gson();
        ArrayList<Point> allPoints = new ArrayList<>(Arrays.asList(gson.fromJson(json, Point[].class)));

        Collections.shuffle(allPoints, random);

        // if clusterCount is less than there are points, add only clusterCount points to the list
        //  if clusterCount is more than there are points, add all points to the list
        for(int i = 0; i < Math.min(clusterCount, allPoints.size()); i++) {
            points.add(allPoints.get(i));
        }

        // this loop can only run when clusterCount is more than there are points
        //  randomly add points from the allPoints list to the list
        for (int i = 0; i < Math.max(0, clusterCount - allPoints.size()); i++) {
            points.add(allPoints.get(random.nextInt(allPoints.size())));
        }
    }



    public ArrayList<Point> getPoints() {
        return points;
    }
}
