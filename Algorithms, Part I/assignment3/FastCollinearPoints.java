import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdDraw;
import edu.princeton.cs.algs4.StdOut;

import java.util.ArrayList;
import java.util.Arrays;

public class FastCollinearPoints {
    private final ArrayList<LineSegment> seg = new ArrayList<>();

    // finds all line segments containing 4 or more points
    public FastCollinearPoints(Point[] points) {
        if (points == null) {
            throw new IllegalArgumentException("Points list cannot be null");
        }

        checkPointsNotNull(points);
        checkPointNotDuplicated(points);

        Point[] pselect = points.clone();
        // Arrays.sort(plist);
        // Point[] pselect = plist.clone();

        for (int i = 0; i < pselect.length - 3; i++) {
            Arrays.sort(pselect);
            Arrays.sort(pselect, pselect[i].slopeOrder());

            int first = 1;
            int last = 2;
            while (last < pselect.length) {
                while (last < pselect.length && Double.compare(pselect[0].slopeTo(pselect[first]),
                                                               pselect[0].slopeTo(pselect[last])) == 0) {
                    last++;
                }

                if (last - first >= 3 && pselect[0].compareTo(pselect[first]) < 0) {
                    seg.add(new LineSegment(pselect[0], pselect[last - 1]));
                }

                first = last;
                last++;
            }
        }

    }


    private static void checkPointsNotNull(Point[] points) {
        for (Point p : points) {
            if (p == null) throw new IllegalArgumentException("Point item cannot be null");
        }
    }

    private static void checkPointNotDuplicated(Point[] points) {
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

    // the line segments
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
        FastCollinearPoints collinear = new FastCollinearPoints(points);
        for (LineSegment segment : collinear.segments()) {
            StdOut.println(segment);
            segment.draw();
        }
        StdDraw.show();
    }
}