package lib;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Texhnolyze
 */
public class MinBinaryHeap<E> {
    
    private E[] heap;
    private Map<E, Integer> indexOf;
    private Comparator<E> cmp;
    
    private int size;
    
    public MinBinaryHeap(Comparator<E> cmp) {
        this(32, cmp);
    }
    
    public MinBinaryHeap(int initCap, Comparator<E> cmp) {
        this.cmp = cmp;
        heap = (E[]) new Object[initCap];
        indexOf = new HashMap<>();
    }
    
    private MinBinaryHeap() {}
    
    public void clear() {
        heap = (E[]) new Object[32];
        indexOf.clear();
        size = 0;
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
    
    public void add(E e) {
        if (indexOf.get(e) != null) 
            return;
        if (size == heap.length) 
            resize(2 * heap.length);
        heap[size] = e;
        indexOf.put(e, size);
        emerge(size, heap, indexOf, cmp);
        size++;
    }
    
    public E popMin() {
        size--;
        E e = heap[0];
        heap[0] = heap[size];
        heap[size] = null;
        indexOf.remove(e);
        indexOf.put(heap[0], 0);
        drown(0, size, heap, indexOf, cmp);
        if (size < heap.length / 4) 
            resize(heap.length / 2);
        return e;
    }
    
    public void priorityChanged(E e) {
        Integer idx = indexOf.get(e);
        if (idx == null) return;
        int parent = parent(idx);
        if (parent != -1 && cmp.compare(e, heap[parent]) < 0) 
            emerge(idx, heap, indexOf, cmp);
        else 
            drown(idx, size, heap, indexOf, cmp);
    }
    
    private void resize(int cap) {
        E[] h = (E[]) new Object[cap];
        System.arraycopy(heap, 0, h, 0, size);
        heap = h;
    }
    
    public static <E> MinBinaryHeap<E> fromArray(E[] arr, Comparator<E> cmp) {
        int N = arr.length;
        for (int i = N / 2; i >= 0; i--) 
            drown(i, N, arr, null, cmp);
        Map<E, Integer> indexOf = new HashMap<>();
        for (int i = 0; i < N; i++) 
            indexOf.put(arr[i], i);
        MinBinaryHeap<E> heap = new MinBinaryHeap<>();
        heap.cmp = cmp;
        heap.heap = arr;
        heap.size = N;
        heap.indexOf = indexOf;
        return heap;
    }
    
    private static <E> void drown(int idx, int size, E[] heap, final Map<E, Integer> indexOf, Comparator<E> cmp) {
        E e = heap[idx];
        int p = idx;
        int left = left(p);
        int right = right(p);
        while (left < size) {
            if (cmp.compare(e, heap[left]) > 0) {
                int lrcmp = right < size ? cmp.compare(heap[left], heap[right]) : -1;
                if (lrcmp <= 0) {
                    heap[p] = heap[left];
                    p = left;
                } else {
                    heap[p] = heap[right];
                    p = right;
                }
            } else {
                if (right < size && cmp.compare(e, heap[right]) > 0) {
                    heap[p] = heap[right];
                    p = right;
                } else
                    break;
            }
            if (indexOf != null) 
                indexOf.put(heap[p], p);
            left = left(p);
            right = right(p);
        }
        heap[p] = e;
        if (indexOf != null) 
            indexOf.put(e, p);
    }
    
    private static <E> void emerge(int idx, E[] heap, final Map<E, Integer> indexOf, Comparator<E> cmp) {
        E e = heap[idx];
        int i = idx;
        int parent = parent(i);
        while (parent != -1) {
            if (cmp.compare(e, heap[parent]) < 0) {
                heap[i] = heap[parent];
                if (indexOf != null) 
                    indexOf.put(heap[i], i);
                i = parent;
                parent = parent(i);
            } else  
                break;
        }
        heap[i] = e;
        if (indexOf != null) 
            indexOf.put(e, i);
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
    
}
