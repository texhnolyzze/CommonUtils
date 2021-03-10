package org.texhnolyzze.common;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 *
 * @author Texhnolyze
 */
public class IndexedMinPriorityQueue<E> {
    
    private E[] heap;
    private final Map<E, Integer> indexOf;
    private final Comparator<E> cmp;
    
    private int size;
    
    public IndexedMinPriorityQueue(Comparator<E> cmp) {
        this(32, cmp);
    }
    
    public IndexedMinPriorityQueue(int initCap, Comparator<E> cmp) {
        this.cmp = cmp;
        heap = (E[]) new Object[initCap];
        indexOf = new HashMap<>(initCap);
    }
    
    private IndexedMinPriorityQueue(E[] heap, Map<E, Integer> indexOf, Comparator<E> cmp, int size) {
        this.heap = heap;
        this.indexOf = indexOf;
        this.cmp = cmp;
        this.size = size;
    }
    
    public void clear() {
        heap = (E[]) new Object[32];
        indexOf.clear();
        size = 0;
    }
    
    public int size() {
        return size;
    }
    
    public boolean isEmpty() {
        return size == 0;
    }
    
    public boolean contains(E e) {
        return indexOf.containsKey(e);
    }
    
    public E peek() {
        return heap[0];
    }
    
    public void add(E e) {
        if (indexOf.containsKey(e)) 
            return;
        if (size == heap.length) 
            resize(2 * heap.length);
        heap[size] = e;
        indexOf.put(e, size);
        emerge(size, heap, indexOf, cmp);
        size++;
    }
    
    public E pop() {
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
    
//  Call this method if the E is mutable type and the state on 
//  which it's ordering depends changed
    public void priorityChanged(E e) {
        Integer idx = indexOf.get(e);
        if (idx == null) 
            return;
        int parent = parent(idx);
        if (parent != -1 && cmp.compare(e, heap[parent]) < 0) 
            emerge(idx, heap, indexOf, cmp);
        else 
            drown(idx, size, heap, indexOf, cmp);
    }
    
    public void remove(E e) {
        Integer idx = indexOf.get(e);
        if (idx == null) 
            return;
        Comparator<E> temp_cmp = (e1, e2) -> -1;
        emerge(idx, heap, indexOf, temp_cmp);
        pop();
    }
    
    private void resize(int cap) {
        E[] h = (E[]) new Object[cap];
        System.arraycopy(heap, 0, h, 0, size);
        heap = h;
    }
    
    public static <E> IndexedMinPriorityQueue<E> fromArray(E[] arr, Comparator<E> cmp) {
        E[] heapArr = Stream.of(arr).distinct().toArray(size -> (E[]) new Object[size]);
        int N = heapArr.length;
        heapify(heapArr, cmp);
        Map<E, Integer> indexOf = new HashMap<>();
        for (int i = 0; i < N; i++) 
            indexOf.put(heapArr[i], i);
        IndexedMinPriorityQueue<E> heap = new IndexedMinPriorityQueue<>(heapArr, indexOf, cmp, N);
        return heap;
    }
    
    public static <E> void heapify(E[] arr, Comparator<E> cmp) {
        int N = arr.length;
        for (int i = N / 2; i >= 0; i--) 
            drown(i, N, arr, null, cmp);
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
                    if (indexOf != null) 
                        indexOf.put(heap[p], p);
                    p = left;
                } else {
                    heap[p] = heap[right];
                    if (indexOf != null)
                        indexOf.put(heap[p], p);
                    p = right;
                }
            } else {
                if (right < size && cmp.compare(e, heap[right]) > 0) {
                    heap[p] = heap[right];
                    if (indexOf != null)
                        indexOf.put(heap[p], p);
                    p = right;
                } else
                    break;
            }
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
