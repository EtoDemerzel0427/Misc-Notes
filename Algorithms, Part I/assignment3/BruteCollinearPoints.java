import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdDraw;
import edu.princeton.cs.algs4.StdOut;

import java.util.ArrayList;
import java.util.Arrays;

public class BruteCollinearPoints {
    private final ArrayList<LineSegment> seg = new ArrayList<>();

    // finds all line segments containing 4 points
    public BruteCollinearPoints(Point[] points) {
        if (points == null) {
            throw new IllegalArgumentException("Points list cannot be null");
        }

        checkPointsNotNull(points);
        checkPointNotDuplicated(points);

        Point[] plist = points.clone();
        Arrays.sort(plist);

        for (int p = 0; p < plist.length - 3; p++) {
            for (int q = p + 1; q < plist.length - 2; q++) {
                for (int r = q + 1; r < plist.length - 1; r++) {
                    for (int s = r + 1; s < plist.length; s++) {
                        if (plist[p].slopeTo(plist[q]) == plist[p].slopeTo(plist[r]) &&
                                plist[p].slopeTo(plist[q]) == plist[p].slopeTo(plist[s])) {
                            seg.add(new LineSegment(plist[p], plist[s]));
                        }
                    }
                }
            }
        }

    }

    private static void checkPointsNotNull(Point[] points) {
        for (Point p: points) {
            if (p == null) throw new IllegalArgumentException("Point item cannot be null");
        }
    }

    private static void checkPointNotDuplicated(Point[] points)  {
        for (int i = 0; i < points.length; i++) {
            for (int j = i + 1; j < points.length; j++) {
                if (points[i].compareTo(points[j]) == 0) {
                    throw new IllegalArgumentException("Points cannot be duplicated.");
                }
            }
        }
    }

    // the number of line segments
    public int numberOfSegments() {
        return seg.size();
    }
    public LineSegment[] segments() {
        return seg.toArray(new LineSegment[0]);
    }

    public static void main(String[] args) {

        // read the n points from a file
        In in = new In(args[0]);
        int n = in.readInt();
        Point[] points = new Point[n];
        for (int i = 0; i < n; i++) {
            int x = in.readInt();
            int y = in.readInt();
            points[i] = new Point(x, y);
        }

        // draw the points
        StdDraw.enableDoubleBuffering();
        StdDraw.setXscale(0, 32768);
        StdDraw.setYscale(0, 32768);
        for (Point p : points) {
            p.draw();
        }
        StdDraw.show();

        // print and draw the line segments
        BruteCollinearPoints collinear = new BruteCollinearPoints(points);
        for (LineSegment segment : collinear.segments()) {
            StdOut.println(segment);
            segment.draw();
        }
        StdDraw.show();
    }
}