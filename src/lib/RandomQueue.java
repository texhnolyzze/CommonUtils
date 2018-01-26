package lib;

import java.util.Iterator;
import java.util.Random;

/**
 *
 * @author Texhnolyze
 */
public class RandomQueue<E> implements Iterable<E> {
    
    private E[] elems;
    private final Random rnd = new Random(System.currentTimeMillis());

    private int size;

    public RandomQueue() {
        this(32);
    }

    public RandomQueue(int initCap) {
        elems = (E[]) new Object[initCap];
    }

    public void clear() {
        size = 0;
        elems = (E[]) new Object[32];
    }
    
    public int getSize() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void add(E e) {
        elems[size++] = e;
        if (size == elems.length) resize(2 * elems.length);
    }

    public E get() {
        int idx = rnd.nextInt(size--);
        E e = elems[idx];
        elems[idx] = elems[size];
        if (size < elems.length / 4) resize(elems.length / 2);
        return e;
    }

    private void resize(int newCap) {
        E[] newElems = (E[]) new Object[newCap];
        System.arraycopy(elems, 0, newElems, 0, size);
        elems = newElems;
    }

    @Override
    public Iterator<E> iterator() {

        return new Iterator<E>() {

            private int idx;
            private int[] idxs = new int[size];

            {

                for (int i = 0; i < size; i++) idxs[i] = i;

                for (int i = 0; i < size; i++) {

                    int next = i + rnd.nextInt(size - i);

                    int temp = idxs[i];
                    idxs[i] = idxs[next];
                    idxs[next] = temp;

                }
            }

            @Override
            public boolean hasNext() {
                return idx < idxs.length;
            }

            @Override
            public E next() {
                return elems[idxs[idx++]];
            }

        };

    }
    
}
