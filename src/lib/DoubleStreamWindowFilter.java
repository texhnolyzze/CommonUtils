package lib;

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
    private interface BiDoublePredicate {
        boolean test(double d1, double d2);
    }

    @FunctionalInterface    
    public interface IntDoubleConsumer {
        void accept(int i, double d);
    }
    
    public static class IntDoublePair {
        public final int i;
        public final double d;
        public IntDoublePair(int i, double d) {this.i = i; this.d = d;}
    }
    
    public enum FilterType {
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
        FixedSizeDoubleDeque dDeque;
        FixedSizeIntegerDeque iDeque;
        switch (filterType) {
            case MIN_FILTER:
            case MAX_FILTER:
                dDeque = new FixedSizeDoubleDeque(windowSize);
                iDeque = new FixedSizeIntegerDeque(windowSize);
                BiDoublePredicate predicate = filterType == FilterType.MIN_FILTER ? (d1, d2) -> d1 > d2 : (d1, d2) -> d1 < d2;
                while (it.hasNext() && i < windowSize) {
                    double next = it.nextDouble();
                    while (dDeque.notEmpty() && predicate.test(dDeque.peekTail(), next)) {
                        dDeque.pollTail();
                        iDeque.pollTail();
                    }
                    dDeque.appendTail(next);
                    iDeque.appendTail(i);
                    i++;
                }
                if (i == windowSize) {
                    consumer.accept(0, dDeque.peekHead());
                    while (it.hasNext()) {
                        double next = it.nextDouble();
                        if (iDeque.peekHead() == i - windowSize) {
                            dDeque.pollHead();
                            iDeque.pollHead();
                        }
                        while (dDeque.notEmpty() && predicate.test(dDeque.peekTail(), next)) {
                            dDeque.pollTail();
                            iDeque.pollTail();
                        }
                        dDeque.appendTail(next);
                        iDeque.appendTail(i);
                        consumer.accept(i - windowSize + 1, dDeque.peekHead());
                        i++;
                    }
                }
                break;
            case AVG_FILTER:                
                dDeque = new FixedSizeDoubleDeque(windowSize);
                double avg = 0.0;
                double wsInv = 1.0D / windowSize;
                while (it.hasNext() && i < windowSize) {
                    double next = it.nextDouble() * wsInv;
                    avg += next;
                    dDeque.appendTail(next);
                    i++;
                }
                if (i == windowSize) {
                    consumer.accept(0, avg);
                    while (it.hasNext()) {
                        double next = it.nextDouble() * wsInv;
                        avg = avg - dDeque.pollHead() + next;
                        dDeque.appendTail(next);
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
        int head;
        int tail;
        FixedSizeDoubleDeque(int maxSize) {arr = new double[maxSize];}
        int size() {return size;}
        boolean notEmpty() {return size != 0;}
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
        int head;
        int tail;
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
        
        void pollHead() {
            size--;
            head = (head + 1) % arr.length;
        }
        
        void pollTail() {
            size--;
            tail = tail == 0 ? arr.length - 1 : tail - 1;
        }
        
    }
    
    private static interface IndexedMedianFinder {
        double getMedian();
        void setOn(int index, double elem);
    }
    
    private static class IndexedOrderedSequenceOfDouble implements IndexedMedianFinder {
        
        final double[] arr;
        final int[] ptr, ptrIdx;
        
        IndexedOrderedSequenceOfDouble(double[] arr) {
            this.arr = arr;
            this.ptr = new int[arr.length];
            this.ptrIdx = new int[arr.length];
            init();
        }
        
        void init() { // insertion sort
            for (int i = 1; i < arr.length; i++) {
                int j = i;
                double ai = arr[i];
                while (j > 0 && ai < arr[ptr[j - 1]]) {
                    ptr[j] = ptr[j - 1];
                    ptrIdx[ptr[j]] = j;
                    j--;
                }
                ptr[j] = i;
                ptrIdx[i] = j;
            }
        }
        
        @Override
        public double getMedian() {
            return arr.length % 2 == 0 ? (arr[ptr[arr.length / 2 - 1]] + arr[ptr[arr.length / 2]]) / 2 : arr[ptr[arr.length / 2]];
        }
        
        @Override
        public void setOn(int index, double elem) {
            arr[index] = elem;
            int i = ptrIdx[index];
            if (i > 0) {
                if (i < arr.length - 1) {
                    if (elem < arr[ptr[i - 1]]) {
                        do {
                            ptr[i] = ptr[i - 1];
                            ptrIdx[ptr[i]] = i;
                            i--;
                        } while (i > 0 && elem < arr[ptr[i - 1]]);
                    } else {
                        while (elem > arr[ptr[i + 1]]) {
                            ptr[i] = ptr[i + 1];
                            ptrIdx[ptr[i]] = i;
                            i++;
                            if (i == arr.length - 1)
                                break;
                        }
                    }
                } else {
                    while (i > 0 && elem < arr[ptr[i - 1]]) {
                        ptr[i] = ptr[i - 1];
                        ptrIdx[ptr[i]] = i;
                        i--;
                    }
                }
            } else {
                while (i < arr.length - 1 && elem > arr[ptr[i + 1]]) {
                    ptr[i] = ptr[i + 1];
                    ptrIdx[ptr[i]] = i;
                    i++;
                }
            }
            ptr[i] = index;
            ptrIdx[index] = i;
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
            Node leftChild;
            Node rightChild;
            Node() {}

            void setSize() {
                size = count + (leftChild == null ? 0 : leftChild.size) + (rightChild == null ? 0 : rightChild.size);
            }

        }
        
        RandomizedBSTOfDouble(double[] arr) {
            this.keys = arr;
            this.pool = new Pool<>(Node::new, node -> {
                node.size = 1;
                node.leftChild = null;
                node.rightChild = null;
            });
            for (double v : arr)
                root = put(v, root);
        }
        
        Node unite(Node l, Node r) {
            if (l == null)
                return r;
            if (r == null)
                return l;
            int n = l.size;
            int m = r.size;
            if (random.nextInt(n + m) < n) {
                l.rightChild = unite(l.rightChild, r);
                l.setSize();
                return l;
            } else {
                r.leftChild = unite(l, r.leftChild);
                r.setSize();
                return r;
            }
        }
        
        Node rotateLeft(Node x) {
            Node y = x.rightChild;
            x.rightChild = y.leftChild;
            y.leftChild = x;
            y.size = x.size;
            x.setSize();
            return y;
        }
        
        Node rotateRight(Node y) {
            Node x = y.leftChild;
            y.leftChild = x.rightChild;
            x.rightChild = y;
            x.size = y.size;
            y.setSize();
            return x;
        }
        
        Node put(double key, Node n) {
            if (n == null) {
                Node x = pool.obtain();
                x.key = key;
                return x;
            }
            if (random.nextInt(n.size + 2) == 0) 
                return insertToRoot(key, n);
            if (key < n.key)
                n.leftChild = put(key, n.leftChild);
            else if (key > n.key)
                n.rightChild = put(key, n.rightChild);
            else 
                n.count++;
            n.size++;
            return n;
        }
        
        Node insertToRoot(double key, Node n) {
            if (n == null) {
                Node x = pool.obtain();
                x.key = key;
                return x;
            }
            n.size++;
            if (key < n.key) {
                n.leftChild = insertToRoot(key, n.leftChild);
                n = rotateRight(n);
            } else if (key > n.key) {
                n.rightChild = insertToRoot(key, n.rightChild);
                n = rotateLeft(n);
            } else 
                n.count++;
            return n;
        }
        
        Node remove(double key, Node n) {
            if (key < n.key)
                n.leftChild = remove(key, n.leftChild);
            else if (key > n.key)
                n.rightChild = remove(key, n.rightChild);
            else {
                if (n.count > 1) 
                    n.count--;
                else {
                    Node union = unite(n.leftChild, n.rightChild);
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
                return (getKthElem(root, keys.length / 2 - 1) + getKthElem(root, keys.length / 2)) / 2;
            else
                return getKthElem(root, keys.length / 2);
        }
        
        private double getKthElem(Node n, int k) {
            if (n.leftChild != null) {
                int index = n.leftChild.size;
                if (index <= k && k <= index + n.count - 1)
                    return n.key;
                if (k < n.leftChild.size)
                    return getKthElem(n.leftChild, k);
                return getKthElem(n.rightChild, k - n.leftChild.size - n.count);
            } else {
                if (k <= n.count - 1)
                    return n.key;
                return getKthElem(n.rightChild, k - n.count);
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
