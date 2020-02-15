import java.util.Iterator;
import java.util.Scanner;

public class Stack<Item> implements Iterable<Item> {
    private Node first;
    private int N;

    private class Node
    {
        Item item;
        Node next;
    }

    public boolean isEmpty() { return first == null; } // or N == 0
    public int size() {return N;}
    public void push(Item item) {
        Node oldfirst = first;
        first = new Node();
        first.item = item;
        first.next = oldfirst;
        N++;
    }

    public Item pop() {
        Item item = first.item;
        first = first.next;
        N--;
        return item;
    }

    public Iterator<Item> iterator() { return new StackIterator(); }

    private class StackIterator implements  Iterator<Item> {
        private Node current = first;

        public boolean hasNext() { return current != null; }
        public void remove() {} // here we do not think letting a iterator have this method is a good idea.
        public Item next() {
            Item item = current.item;
            current = current.next;
            return item;
        }
    }

    public static void main(String[] args) {
        Stack<String> s = new Stack<String>();

        var in = new Scanner(System.in);
        while (in.hasNext()) {
            String item = in.next();
            if (!item.equals("-")) {
                s.push(item);
            }
            else if (!s.isEmpty()) {
                System.out.print(s.pop() + " ");
            }
        }

        System.out.println("(" + s.size() + " left on stack)");

    }
}
