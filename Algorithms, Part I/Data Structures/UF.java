import java.util.Arrays;

/* weighted disjoint set. */
public class UF {
    private int[] id;
    private int[] size;
    private int N;
    private int count;

    public UF(int N) {
        this.N = N;
        count = N;

        id = new int[this.N];
        size = new int[this.N];

        Arrays.fill(size, 1);
        for (int i = 0; i < N; i++)
            id[i] = i;
    }

    /**
     * Merges the component containing site {@code p} with the
     * the component containing site {@code q}.
     */
    public void union(int p, int q) {
        validate(p);
        validate(q);

        int a = find(p);
        int b = find(q);
        if (a == b) return;

        if (size[a] > size[b]) {
            id[b] = a;
            size[a] += size[b];
        }
        else {
            id[a] = b;
            size[b] += size[a];
        }

        count--;
    }

    // Returns the component identifier for the component containing site {@code p}.
    public int find(int p) {
        validate(p);

        int root = p;
        while (root != id[root]) {
            root = id[root];
        }

        while (p != id[p]) {
            int temp = id[p];
            id[p] = root;
            p = temp;
        }

        return root;
    }

    // Returns true if the the two sites are in the same component.
    public boolean connected(int p, int q) {
        validate(p);
        validate(q);
        return find(p) == find(q);
    }

    // return the number of connected conponents.
    public int count() {
        return count;
    }

    public void printRoots() {
        for (int i = 0; i < N; i++) {
            System.out.println("Root of node " + i + ": " + find(i));
        }
    }

    private void validate(int p) {
        if (p < 0 || p >= N) {
            throw new IllegalArgumentException("index " + p + " is not between 0 and " + (N-1));
        }
    }

    public static void main(String[] args) {
        int N = 12;
        var uf = new UF(N);
        System.out.println(uf.count());
        uf.union(3, 7);
        uf.union(4,6);
        uf.union(5, 4);
        uf.union(5, 3);
        uf.union(6,2);
        uf.union(5, 9);
        System.out.println(uf.count());
        uf.printRoots();
    }
}

