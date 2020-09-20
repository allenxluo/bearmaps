package bearmaps.utils.ps;

import java.util.Collections;
import java.util.List;

public class KDTree implements PointSet {

    private class Node {

        Point item;
        Node left;
        Node right;
        Boolean axis; // true if on x-axis level

        public Node(Point item, Boolean axis) {
            this.item = item;
            this.axis = axis;
            left = right = null;
        }

        public Node(Point item, Node left, Node right, Boolean axis) {
            this.item = item;
            this.left = left;
            this.right = right;
            this.axis = axis;
        }
    }

    private Node root;

    public KDTree(List<Point> points) {
        root = KDTreeConstructor(points, true);
    }

    private int sortX(Point p1, Point p2) {
        return Double.compare(p1.getX(), p2.getX());
    }

    private int sortY(Point p1, Point p2) {
        return Double.compare(p1.getY(), p2.getY());
    }

    private Node KDTreeConstructor(List<Point> points, boolean axis) {
        if (points.size() == 0) {
            return null;
        }
        if (points.size() == 1) {
            return new Node(points.get(0), axis);
        }
        if (axis) {
            Collections.sort(points, this::sortX);
            axis = false;
        } else {
            Collections.sort(points, this::sortY);
            axis = true;
        }
        int midIndex = points.size() / 2;
        return new Node(points.get(midIndex), KDTreeConstructor(points.subList(0, midIndex), axis), KDTreeConstructor(points.subList(midIndex + 1, points.size()), axis), !axis);
    }

    public Point nearest(double x, double y) {
        return nearestHelper(new Point(x, y), root, root.item);
    }

    private Point nearestHelper(Point p, Node n, Point nearest) {
        if (n == null) {
            return nearest;
        }
        if (Point.distance(p, n.item) < Point.distance(p, nearest)) {
            nearest = n.item;
        }
        Point nearestLeft;
        Point nearestRight;
        if (n.axis) {
            if (p.getX() <= n.item.getX()) {
                nearestLeft = nearestHelper(p, n.left, nearest);
                if (Point.distance(p, nearestLeft) < Point.distance(p, nearest)) {
                    nearest = nearestLeft;
                }
                if (Point.distance(p, new Point(n.item.getX(), p.getY())) < Point.distance(p, nearest)) {
                    nearestRight = nearestHelper(p, n.right, nearest);
                } else {
                    nearestRight = nearest;
                }
            } else {
                nearestRight = nearestHelper(p, n.right, nearest);
                if (Point.distance(p, nearestRight) < Point.distance(p, nearest)) {
                    nearest = nearestRight;
                }
                if (Point.distance(p, new Point(n.item.getX(), p.getY())) < Point.distance(p, nearest)) {
                    nearestLeft = nearestHelper(p, n.left, nearest);
                } else {
                    nearestLeft = nearest;
                }
            }
        } else {
            if (p.getY() <= n.item.getY()) {
                nearestLeft = nearestHelper(p, n.left, nearest);
                if (Point.distance(p, nearestLeft) < Point.distance(p, nearest)) {
                    nearest = nearestLeft;
                }
                if (Point.distance(p, new Point(p.getX(), n.item.getY())) < Point.distance(p, nearest)) {
                    nearestRight = nearestHelper(p, n.right, nearest);
                } else {
                    nearestRight = nearest;
                }
            } else {
                nearestRight = nearestHelper(p, n.right, nearest);
                if (Point.distance(p, nearestRight) < Point.distance(p, nearest)) {
                    nearest = nearestRight;
                }
                if (Point.distance(p, new Point(p.getX(), n.item.getY())) < Point.distance(p, nearest)) {
                    nearestLeft = nearestHelper(p, n.left, nearest);
                } else {
                    nearestLeft = nearest;
                }
            }
        }
        if (Point.distance(p, nearestLeft) < Point.distance(p, nearest)) {
            nearest = nearestLeft;
        }
        if (Point.distance(p, nearestRight) < Point.distance(p, nearest)) {
            nearest = nearestRight;
        }
        return nearest;
    }
}
