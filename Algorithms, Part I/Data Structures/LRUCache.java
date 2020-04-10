import java.util.HashMap;
import java.util.Map;

public class LRUCache {
    private class Node {
        Node prev;
        Node next;
        int key;
        int val;

        public Node(int k, int v) {
            key = k;
            val = v;
            prev = null;
            next = null;
        }

        // for dummy nodes
        public Node() {
            key = 0;
            val = 0;
            prev = null;
            next = null;
        }

        public void setVal(int v) {
            val = v;
        }
    }

    private int _capacity;
    private int count;
    private Map<Integer, Node> map;
    Node head;
    Node tail;

    public LRUCache(int capacity) {
        _capacity = capacity;
        count = 0;
        map = new HashMap<>();

        head = new Node();
        tail = new Node();
        head.next = tail;
        tail.prev = head;
    }

    public int get(int key) {
        var res = map.getOrDefault(key, null);
        if (res == null) return -1;

        // move to head
        res.prev.next = res.next;
        res.next.prev = res.prev;
        res.prev = head;
        res.next = head.next;
        head.next = res;
        res.next.prev = res;

        return res.val;
    }

    public void put(int key, int value) {
        if (map.containsKey(key)) {
            var res = map.get(key);
            res.setVal(value);

            res.prev.next = res.next;
            res.next.prev = res.prev;
            res.prev = head;
            res.next = head.next;
            head.next = res;
            res.next.prev = res;

            return;
        }
        if (count >= _capacity) {
            // remove the last, i.e, tail.prev
            map.remove(tail.prev.key);
            tail.prev.prev.next = tail;
            tail.prev = tail.prev.prev;
            count--;
        }

        var res = new Node(key, value);
        res.prev = head;
        res.next = head.next;
        head.next = res;
        res.next.prev = res;

        map.put(key, res);
        count++;
    }

    public void printMap() {
        System.out.println(map.toString());
    }

    public static void main(String[] args) {
        LRUCache cache = new LRUCache( 2 /* capacity */ );

        cache.put(2, 1);
        cache.put(1, 1);
        cache.printMap();
        cache.put(2, 3);
        cache.printMap();
        cache.put(4, 1);
        System.out.println(cache.get(1));
        System.out.println(cache.get(2));       // returns 1
//        cache.put(3, 3);    // evicts key 2
//        System.out.println(cache.get(2));       // returns -1 (not found)
//        cache.put(4, 4);    // evicts key 1
//        System.out.println(cache.get(1));       // returns -1 (not found)
//        System.out.println(cache.get(3));       // returns 3
//        System.out.println(cache.get(4));       // returns 4
    }
}
