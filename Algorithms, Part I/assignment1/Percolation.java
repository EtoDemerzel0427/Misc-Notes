import edu.princeton.cs.algs4.WeightedQuickUnionUF;

public class Percolation {
    private final int n;
    private final int top;
    private final int bottom;
    private boolean[][] grid;
    private int opensites;
    private WeightedQuickUnionUF uf;
    private WeightedQuickUnionUF ufFull;


    // creates n-by-n grid, with all sites initially blocked
    public Percolation(int n) {
        if (n <= 0) {
            throw new IllegalArgumentException("n must be positive.");
        }

        this.n = n;
        top = n * n;
        bottom = n * n + 1;
        opensites = 0;
        grid = new boolean[n][n]; // all false, so initialized all blocked sites
        uf = new WeightedQuickUnionUF(n * n + 2); // including top and bottom
        ufFull = new WeightedQuickUnionUF(n * n + 1); // including top
    }

    // opens the site (row, col) if it is not open already
    public void open(int row, int col) {
        if (!isValidPos(row, col)) {
            throw new IllegalArgumentException("Invalid grid coordinates.");
        }

        if (isOpen(row, col)) return;

        // set it opened
        grid[row - 1][col - 1] = true;
        opensites++;

        // union with its neighbors
        int index = flatten(row, col);
        if (row - 1 >= 1) {
            if (isOpen(row - 1, col)) {
                uf.union(index, flatten(row - 1, col));
                ufFull.union(index, flatten(row - 1, col));
            }
        }
        else {
            uf.union(top, index);
            ufFull.union(top, index);
        }

        if (row + 1 <= n) {
            if (isOpen(row + 1, col)) {
                uf.union(index, flatten(row + 1, col));
                ufFull.union(index, flatten(row + 1, col));
            }
        }
        else {
            uf.union(bottom, index);
        }

        if (col - 1 >= 1 && isOpen(row, col - 1)) {
            uf.union(index, flatten(row, col - 1));
            ufFull.union(index, flatten(row, col - 1));
        }

        if (col + 1 <= n && isOpen(row, col + 1)) {
            uf.union(index, flatten(row, col + 1));
            ufFull.union(index, flatten(row, col + 1));
        }
    }

    // is the site (row, col) open?
    public boolean isOpen(int row, int col) {
        if (!isValidPos(row, col)) {
            throw new IllegalArgumentException("Invalid grid coordinates.");
        }
        return grid[row - 1][col - 1];
    }


    // is the site (row, col) full?
    public boolean isFull(int row, int col) {
        if (!isValidPos(row, col)) {
            throw new IllegalArgumentException("Invalid grid coordinates.");
        }
        return ufFull.connected(top, flatten(row, col)); // ufFull.find(top) == ufFull.find(flatten(row, col));
    }

    // returns the number of open sites
    public int numberOfOpenSites() {
        return opensites;
    }

    // does the system percolate?
    public boolean percolates() {
        return uf.connected(top, bottom);  //uf.find(top) == uf.find(bottom);
    }

    // check whether the current position is part of the grids
    private boolean isValidPos(int row, int col) {
        return (row >= 1 && row <= n && col >= 1 && col <= n);
    }

    private int flatten(int row, int col) {
        return (row - 1) * n + col - 1;
    }

    // test client (optional)
    public static void main(String[] args) {
        Percolation perc = new Percolation(5);

        System.out.println(perc.isValidPos(1, 1));

    }
}