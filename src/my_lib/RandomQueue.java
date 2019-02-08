package my_lib;

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
    
    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void add(E e) {
        if (size == elems.length) 
            resize(2 * elems.length);
        elems[size++] = e;
    }

    public E poll() {
        int idx = rnd.nextInt(size--);
        E e = elems[idx];
        elems[idx] = elems[size];
        elems[size] = null;
        if (size < elems.length / 4) 
            resize(elems.length / 2);
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
            
            private int index;
            
            @Override public boolean hasNext() {return index < size;}
            @Override public E next() {
                int j = index + rnd.nextInt(size - index);
                E temp = elems[index];
                elems[index] = elems[j];
                elems[j] = temp;
                return elems[index++];
            }

        };
    }
    
}
