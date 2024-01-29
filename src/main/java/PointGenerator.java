import com.google.gson.Gson;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class PointGenerator {

    private ArrayList<Point> points = new ArrayList<>();



    public PointGenerator(int k) {

        String json = null;
        try {
            byte[] jsonData = Files.readAllBytes(Paths.get("src/main/resources/germany.json"));
            json = new String(jsonData);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Gson gson = new Gson();
        ArrayList<Point> allPoints = new ArrayList<Point>(Arrays.asList(gson.fromJson(json, Point[].class)));

        for(int i = 0; i < k; i++) {
            Random random = new Random();
            points.add(allPoints.get(random.nextInt(allPoints.size())));
        }
    }



    public ArrayList<Point> getPoints() {
        return points;
    }
}
