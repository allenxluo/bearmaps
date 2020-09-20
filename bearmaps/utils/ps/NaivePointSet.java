package bearmaps.utils.ps;

import java.util.List;

public class NaivePointSet implements PointSet {

    List<Point> points;

    public NaivePointSet(List<Point> points) {
        this.points = points;
    }

    public Point nearest(double x, double y) {
        Point nearest = points.get(0);
        for (Point p : points) {
            if (Point.distance(p, new Point(x, y)) < Point.distance(nearest, new Point(x, y))) {
                nearest = p;
            }
        }
        return nearest;
    }
}
