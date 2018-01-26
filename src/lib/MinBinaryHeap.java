package lib;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Texhnolyze
 */
public class MinBinaryHeap<E> {
    
    private E[] heap;
    private double[] priority;

    private final Map<E, Integer> indexOf = new HashMap<>();

    private int size;

    public MinBinaryHeap() {this(32);}

    public MinBinaryHeap(int initCap) {
        heap = (E[]) new Object[initCap];
        priority = new double[initCap];
    }
    
    public void clear() {
        size = 0;
        heap = (E[]) new Object[32];
        priority = new double[32];
        indexOf.clear();
    }

    public int getSize() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public E peekMin() {
        return heap[0];
    }

    public double getMinPriority() {
        return priority[0];
    }

    public void add(E e, double p) {

        if (indexOf.get(e) != null) return; 

        heap[size] = e;
        priority[size] = p;
        indexOf.put(e, size);
        emerge(size);
        size++;

        if (size == heap.length) resize(2 * heap.length);

    }

    public E popMin() {
        size--;

        E e = heap[0];
        heap[0] = heap[size];
        heap[size] = null;
        priority[0] = priority[size];

        indexOf.remove(e);
        indexOf.put(heap[0], 0);

        drown(0);

        if (size < heap.length / 4) resize(heap.length / 2);

        return e;
    }

    private void emerge(int idx) {

        E e = heap[idx];
        double cp = priority[idx];

        int i = idx;
        int parent = parent(i);

        while (parent != -1) {
            double pp = priority[parent];
            if (pp > cp) {

                heap[i] = heap[parent];
                priority[i] = pp;
                indexOf.put(heap[i], i);

                i = parent;
                parent = parent(i);

            } else  
                break;
        }

        heap[i] = e;
        priority[i] = cp;
        indexOf.put(e, i);

    }

    private void drown(int idx) {

        E e = heap[idx];
        double pp = priority[idx];

        int p = idx;
        int left = left(p);
        int right = right(p);

        while (left < size) {

            double lcp = priority[left];
            double rcp = right < size ? priority[right] : Double.POSITIVE_INFINITY;

            if (pp > lcp || pp > rcp) {

                if (lcp <= rcp) {

                    heap[p] = heap[left];
                    priority[p] = lcp;

                    p = left;

                } else {

                    heap[p] = heap[right];
                    priority[p] = rcp;

                    p = right;

                }

                indexOf.put(heap[p], p);

                left = left(p);
                right = right(p);

            } else 
                break;

        }

        heap[p] = e;
        priority[p] = pp;
        indexOf.put(e, p);

    }

    public void changePriority(E e, double p) {
        int idx = indexOf.get(e);
        priority[idx] = p;
        int parent = parent(idx);
        if (parent != -1) {
            double pp = priority[parent(idx)];
            if (pp > p) 
                emerge(idx);
        } else 
            drown(idx);
    }

    private static int parent(int idx) {
        return (idx >> 1) + ((idx % 2) - 1);
    }

    private static int left(int idx) {
        return (idx << 1) + 1;
    }

    private static int right(int idx) {
        return (idx << 1) + 2;
    }

    private void resize(int cap) {
        E[] h = (E[]) new Object[cap];
        double[] p = new double[cap];
        System.arraycopy(heap, 0, h, 0, size);
        System.arraycopy(priority, 0, p, 0, size);
        heap = h;
        priority = p;
    }
    
}
