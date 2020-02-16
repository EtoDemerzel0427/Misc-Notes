import edu.princeton.cs.algs4.StdRandom;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class RandomizedQueue<Item> implements Iterable<Item> {
    private Item[] q;
    private int N;

    // construct an empty randomized queue
    public RandomizedQueue() {
        q = (Item[]) new Object[1];
        N = 0;
    }

    // is the randomized queue empty?
    public boolean isEmpty() { return N == 0; }

    // return the number of items on the randomized queue
    public int size() { return N; }

    private void resize(int cap) {
        Item[] temp = (Item[]) new Object[cap];
        for (int i = 0; i < N; i++) {
            temp[i] = q[i];
        }

        q = temp;
    }
    // add the item
    public void enqueue(Item item) {
        if (item == null) {
            throw new IllegalArgumentException("Items should not be null");
        }

        if (N == q.length) resize(2 * q.length);
        q[N++] = item;
    }

    // remove and return a random item
    public Item dequeue() {
        if (isEmpty()) {
            throw new NoSuchElementException("Cannot dequeue from an empty queue.");
        }

        if (N == q.length / 4) {
            resize(q.length / 2);
        }

        int idx = StdRandom.uniform(N);
        Item item = q[idx];
        q[idx] = q[--N];
        q[N] = null; // for garbage collection

        return item;
    }

    // return a random item (but do not remove it)
    public Item sample() {
        if (isEmpty()) {
            throw new NoSuchElementException("Cannot dequeue from an empty queue.");
        }

        int idx = StdRandom.uniform(N);

        return q[idx];
    }

    // return an independent iterator over items in random order
    public Iterator<Item> iterator() {
        return new QueueIterator();
    }

    private class QueueIterator implements Iterator<Item> {
        private final int[] perm = StdRandom.permutation(N);
        private int current = 0;

        public boolean hasNext() {
            return current != perm.length;
        }

        public void remove() {
            throw new UnsupportedOperationException("We do not allow remove.");
        }

        public Item next() {
            if (!hasNext()) {
                throw new NoSuchElementException("There is no more elements");
            }

            return q[perm[current++]];
        }
    }

    // unit testing (required)
    public static void main(String[] args) {
        RandomizedQueue<String> q = new RandomizedQueue<>();
        q.enqueue("one");
        q.enqueue("two");
        q.enqueue("three");
        q.enqueue("four");
        q.enqueue("five");
        for (String item: q) {
            System.out.print(item + " ");
        }

        System.out.println();

        System.out.println("We are removing:" + q.dequeue());

        for (String item: q) {
            System.out.println(item);
        }

        System.out.println("We are moving: " + q.dequeue());

        for (String item: q) {
            System.out.print(item + " ");
        }

        System.out.println();

    }
}