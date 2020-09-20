package bearmaps.utils.ps;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;

public class KDTreeTest {

    @Test
    public void testNearestDemo() {
        List<Point> points = new ArrayList<>();
        points.add(new Point(4, 4));
        points.add(new Point(3, 3));
        points.add(new Point(4, 5));
        points.add(new Point(4, 2));
        points.add(new Point(2, 3));
        points.add(new Point(1, 5));

        KDTree kdtree = new KDTree(points);
        NaivePointSet nps = new NaivePointSet(points);

        assertEquals(kdtree.nearest(0, 7), nps.nearest(0, 7));
    }

    @Test
    public void testLargeRandomSampleSize() {
        for (int i = 0; i < 100; i++) {
            List<Point> points = new ArrayList<>();
            Random generator = new Random();

            for (int j = 0; j < 100; j++) {
                points.add(new Point(generator.nextDouble() * 100, generator.nextDouble() * 100));
            }

            KDTree kdtree = new KDTree(points);
            NaivePointSet nps = new NaivePointSet(points);

            double x = generator.nextDouble() * 100;
            double y = generator.nextDouble() * 100;

            assertEquals(kdtree.nearest(x, y), nps.nearest(x, y));
            System.out.println(nps.nearest(x, y));
        }
    }
}