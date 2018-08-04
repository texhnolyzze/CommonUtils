package lib;

import java.util.Arrays;
import java.util.PrimitiveIterator;
import java.util.Random;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

/**
 *
 * @author Texhnolyze
 */
public class DoubleStreamWindowFilter {
    
    @FunctionalInterface
    private static interface BiDoublePredicate {
        boolean test(double d1, double d2);
    }

    @FunctionalInterface    
    public static interface IntDoubleConsumer {
        void accept(int i, double d);
    }
    
    public static class IntDoublePair {
        public final int i;
        public final double d;
        public IntDoublePair(int i, double d) {this.i = i; this.d = d;}
    }
    
    public static enum FilterType {
        MIN_FILTER, MAX_FILTER, AVG_FILTER, MEDIAN_FILTER
    }
    
    public static Stream<IntDoublePair> filter(FilterType filterType, DoubleStream ds, int windowSize) {
        Stream.Builder<IntDoublePair> builder = Stream.builder();
        filterTerminal(filterType, ds, windowSize, (windowIndex, res) -> builder.accept(new IntDoublePair(windowIndex, res)));
        return builder.build();
    }
    
    public static void filterTerminal(FilterType filterType, DoubleStream ds, int windowSize, IntDoubleConsumer consumer) {
        if (windowSize <= 1)
            throw new IllegalArgumentException("Window size must be >= 2");
        int i = 0;
        PrimitiveIterator.OfDouble it = ds.iterator();
        FixedSizeDoubleDeque d_deque;
        FixedSizeIntegerDeque i_deque;
        switch (filterType) {
            case MIN_FILTER:
            case MAX_FILTER:
                d_deque = new FixedSizeDoubleDeque(windowSize);
                i_deque = new FixedSizeIntegerDeque(windowSize);
                BiDoublePredicate predicate = filterType == FilterType.MIN_FILTER ? (d1, d2) -> d1 > d2 : (d1, d2) -> d1 < d2;
                while (it.hasNext() && i < windowSize) {
                    double next = it.nextDouble();
                    while (!d_deque.isEmpty() && predicate.test(d_deque.peekTail(), next)) {
                        d_deque.pollTail();
                        i_deque.pollTail();
                    }
                    d_deque.appendTail(next);
                    i_deque.appendTail(i);
                    i++;
                }
                if (i == windowSize) {
                    consumer.accept(0, d_deque.peekHead());
                    while (it.hasNext()) {
                        double next = it.nextDouble();
                        if (i_deque.peekHead() == i - windowSize) {
                            d_deque.pollHead();
                            i_deque.pollHead();
                        }
                        while (!d_deque.isEmpty() && predicate.test(d_deque.peekTail(), next)) {
                            d_deque.pollTail();
                            i_deque.pollTail();
                        }
                        d_deque.appendTail(next);
                        i_deque.appendTail(i);
                        consumer.accept(i - windowSize + 1, d_deque.peekHead());
                        i++;
                    }
                }
                break;
            case AVG_FILTER:                
                d_deque = new FixedSizeDoubleDeque(windowSize);
                double avg = 0.0;
                double ws_inv = 1.0D / windowSize;
                while (it.hasNext() && i < windowSize) {
                    double next = it.nextDouble() * ws_inv;
                    avg += next;
                    d_deque.appendTail(next);
                    i++;
                }
                if (i == windowSize) {
                    consumer.accept(0, avg);
                    while (it.hasNext()) {
                        double next = it.nextDouble() * ws_inv;
                        avg = avg - d_deque.pollHead() + next;
                        d_deque.appendTail(next);
                        consumer.accept(i - windowSize + 1, avg);
                        i++;
                    }
                }
                break;
            case MEDIAN_FILTER:
                double[] arr = new double[windowSize];
                while (it.hasNext() && i < windowSize)
                    arr[i++] = it.nextDouble();
                if (i == windowSize) {
                    IndexedMedianFinder medianFinder = windowSize <= 30 ? new IndexedOrderedSequenceOfDouble(arr) : new RandomizedBSTOfDouble(arr);
                    consumer.accept(0, medianFinder.getMedian());
                    while (it.hasNext()) {
                        medianFinder.setOn(i % windowSize, it.nextDouble());
                        consumer.accept(i - windowSize + 1, medianFinder.getMedian());
                        i++;
                    }
                }
                break;
        }
    }
    
    private static class FixedSizeDoubleDeque {
        int size;
        double[] arr;
        int head, tail;
        FixedSizeDoubleDeque(int maxSize) {arr = new double[maxSize];}
        int size() {return size;}
        boolean isEmpty() {return size == 0;}
        double peekHead() {return arr[head];}
        double peekTail() {return arr[tail == 0 ? arr.length - 1 : tail - 1];}
        
        void appendTail(double d) {
            size++;
            arr[tail] = d;
            tail = (tail + 1) % arr.length;
        }
        
        double pollHead() {
            size--;
            double d = arr[head];
            head = (head + 1) % arr.length;
            return d;
        }
        
        double pollTail() {
            size--;
            tail = tail == 0 ? arr.length - 1 : tail - 1;
            return arr[tail];
        }
        
    }
    
    private static class FixedSizeIntegerDeque {
        int size;
        int[] arr;
        int head, tail;
        FixedSizeIntegerDeque(int maxSize) {arr = new int[maxSize];}
        int size() {return size;}
        boolean isEmpty() {return size == 0;}
        int peekHead() {return arr[head];}
        int peekTail() {return arr[tail == 0 ? arr.length - 1 : tail - 1];}
        
        void appendTail(int i) {
            size++;
            arr[tail] = i;
            tail = (tail + 1) % arr.length;
        }
        
        int pollHead() {
            size--;
            int i = arr[head];
            head = (head + 1) % arr.length;
            return i;
        }
        
        int pollTail() {
            size--;
            tail = tail == 0 ? arr.length - 1 : tail - 1;
            return arr[tail];
        }
        
    }
    
    private static interface IndexedMedianFinder {
        double getMedian();
        void setOn(int index, double elem);
    }
    
    private static class IndexedOrderedSequenceOfDouble implements IndexedMedianFinder {
        
        final double[] arr;
        final int[] ptr, ptr_idx;
        
        IndexedOrderedSequenceOfDouble(double[] arr) {
            this.arr = arr;
            this.ptr = new int[arr.length];
            this.ptr_idx = new int[arr.length];
            init();
        }
        
        void init() { // insertion sort
            for (int i = 1; i < arr.length; i++) {
                int j = i;
                double ai = arr[i];
                while (j > 0 && ai < arr[ptr[j - 1]]) {
                    ptr[j] = ptr[j - 1];
                    ptr_idx[ptr[j]] = j;
                    j--;
                }
                ptr[j] = i;
                ptr_idx[i] = j;
            }
        }
        
        @Override
        public double getMedian() {
            return arr.length % 2 == 0 ? (arr[ptr[arr.length / 2 - 1]] + arr[ptr[arr.length / 2]]) / 2 : arr[ptr[arr.length / 2]];
        }
        
        @Override
        public void setOn(int index, double elem) {
            arr[index] = elem;
            int i = ptr_idx[index];
            if (i > 0) {
                if (i < arr.length - 1) {
                    if (elem < arr[ptr[i - 1]]) {
                        do {
                            ptr[i] = ptr[i - 1];
                            ptr_idx[ptr[i]] = i;
                            i--;
                        } while (i > 0 && elem < arr[ptr[i - 1]]);
                    } else {
                        while (elem > arr[ptr[i + 1]]) {
                            ptr[i] = ptr[i + 1];
                            ptr_idx[ptr[i]] = i;
                            i++;
                            if (i == arr.length - 1)
                                break;
                        }
                    }
                } else {
                    while (i > 0 && elem < arr[ptr[i - 1]]) {
                        ptr[i] = ptr[i - 1];
                        ptr_idx[ptr[i]] = i;
                        i--;
                    }
                }
            } else {
                while (i < arr.length - 1 && elem > arr[ptr[i + 1]]) {
                    ptr[i] = ptr[i + 1];
                    ptr_idx[ptr[i]] = i;
                    i++;
                }
            }
            ptr[i] = index;
            ptr_idx[index] = i;
        }
        
    }
    
    private static class RandomizedBSTOfDouble implements IndexedMedianFinder {
        
        final Random random = new Random(System.nanoTime());
        final Pool<Node> pool;
        
        Node root;        
        final double[] keys;
        
        static class Node {
            double key;
            int size = 1;
            int count = 1; // number of equal keys in this node (duplicates)
            Node left_child, right_child;
            Node() {}
            Node(double key) {this.key = key;}
            void set_size() {
                size = count + (left_child == null ? 0 : left_child.size) + (right_child == null ? 0 : right_child.size);
            }
        }
        
        RandomizedBSTOfDouble(double[] arr) {
            this.keys = arr;
            this.pool = Pool.newFixedPool(() -> new Node(), n -> {
                n.size = 1;
                n.left_child = null;
                n.right_child = null;
            }, arr.length);
            for (int i = 0; i < arr.length; i++) 
                root = put(arr[i], root);
        }
        
        Node unite(Node l, Node r) {
            if (l == null)
                return r;
            if (r == null)
                return l;
            int n = l.size;
            int m = r.size;
            if (random.nextInt(n + m) < n) {
                l.right_child = unite(l.right_child, r);
                l.set_size();
                return l;
            } else {
                r.left_child = unite(l, r.left_child);
                r.set_size();
                return r;
            }
        }
        
        Node rotate_left(Node x) {
            Node y = x.right_child;
            if (y == null)
                return x;
            x.right_child = y.left_child;
            y.left_child = x;
            y.size = x.size;
            x.set_size();
            return y;
        }
        
        Node rotate_right(Node y) {
            Node x = y.left_child;
            if (x == null)
                return y;
            y.left_child = x.right_child;
            x.right_child = y;
            x.size = y.size;
            y.set_size();
            return x;
        }
        
        Node put(double key, Node n) {
            if (n == null) {
                Node x = pool.obtain();
                x.key = key;
                return x;
            }
            if (random.nextInt(n.size + 2) == 0) 
                return insert_to_root(key, n);
            if (key < n.key)
                n.left_child = put(key, n.left_child);
            else if (key > n.key)
                n.right_child = put(key, n.right_child);
            else 
                n.count++;
            n.size++;
            return n;
        }
        
        Node insert_to_root(double key, Node n) {
            if (n == null) {
                Node x = pool.obtain();
                x.key = key;
                return x;
            }
            n.size++;
            if (key < n.key) {
                n.left_child = insert_to_root(key, n.left_child);
                n = rotate_right(n);
            } else if (key > n.key) {
                n.right_child = insert_to_root(key, n.right_child);
                n = rotate_left(n);
            } else 
                n.count++;
            return n;
        }
        
        Node remove(double key, Node n) {
            if (key < n.key)
                n.left_child = remove(key, n.left_child);
            else if (key > n.key)
                n.right_child = remove(key, n.right_child);
            else {
                if (n.count > 1) 
                    n.count--;
                else {
                    Node union = unite(n.left_child, n.right_child);
                    pool.free(n);
                    return union;
                }
            }
            n.size--;
            return n;
        }   
        
        @Override
        public double getMedian() {
            if (keys.length % 2 == 0)
                return (get_kth_elem(root, keys.length / 2 - 1) + get_kth_elem(root, keys.length / 2)) / 2;
            else
                return get_kth_elem(root, keys.length / 2);
        }
        
        private double get_kth_elem(Node n, int k) {
            if (n.left_child != null) {
                int n_index = n.left_child.size;
                if (n_index <= k && k <= n_index + n.count - 1)
                    return n.key;
                if (k < n.left_child.size) 
                    return get_kth_elem(n.left_child, k);
                return get_kth_elem(n.right_child, k - n.left_child.size - n.count);
            } else {
                if (k <= n.count - 1)
                    return n.key;
                return get_kth_elem(n.right_child, k - n.count);
            }
        }

        @Override
        public void setOn(int index, double elem) {
            root = remove(keys[index], root);
            root = put(elem, root);
            keys[index] = elem;
        }
        
    }
    
}
