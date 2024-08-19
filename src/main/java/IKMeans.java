import java.util.List;

// interface all K-means modes have to implement
//  this helps with structuring code in the Main class
public interface IKMeans {

    void update();

    List<Point> getCentroids();

    List<List<Point>> getClusters();

}
