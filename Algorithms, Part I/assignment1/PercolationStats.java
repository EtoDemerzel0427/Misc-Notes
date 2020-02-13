import edu.princeton.cs.algs4.StdRandom;
import edu.princeton.cs.algs4.StdStats;

public class PercolationStats {
    private final double mean;
    private final double stdDev;
    private final double lowConfidence;
    private final double highConfidence;


    // perform independent trials on an n-by-n grid
    public PercolationStats(int n, int trials) {
        if (n <= 0 || trials <= 0) {
            throw new IllegalArgumentException("N and T must be <= 0");
        }

        double[] trialResults = new double[trials];

        for (int trial = 0; trial < trials; trial++) {
            Percolation perc = new Percolation(n);
            while (!perc.percolates()) {
                int row = StdRandom.uniform(1, n + 1);
                int col = StdRandom.uniform(1, n + 1);
                perc.open(row, col);
            }

            int openSites = perc.numberOfOpenSites();
            double result = (double) openSites / (n * n);
            trialResults[trial] = result;
        }

        mean = StdStats.mean(trialResults);
        stdDev = StdStats.stddev(trialResults);
        double confidenceFraction = (1.96 * stddev() / Math.sqrt(trials));
        lowConfidence = mean - confidenceFraction;
        highConfidence = mean + confidenceFraction;

    }

    // sample mean of percolation threshold
    public double mean() {
        return mean;
    }

    // sample standard deviation of percolation threshold
    public double stddev() {
        return stdDev;
    }

    // low endpoint of 95% confidence interval
    public double confidenceLo() {
        return lowConfidence;
    }

    // high endpoint of 95% confidence interval
    public double confidenceHi() {
        return highConfidence;
    }

    // test client (see below)
    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        int trials = Integer.parseInt(args[1]);

        PercolationStats percStats = new PercolationStats(n, trials);
        System.out.printf("mean                    = %f\n", percStats.mean());
        System.out.printf("stddev                  = %f\n", percStats.stddev());
        System.out.printf("95%% confidence interval = [%f, %f]\n", percStats.confidenceLo(), percStats.confidenceHi());

    }
}