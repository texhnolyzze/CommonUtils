package lib;

import java.util.Objects;

/**
 *
 * @author Texhnolyze
 */
public class Pool<E> {
    
    private final Pool.Factory<E> factory;
    private final Stack<E> pool = new Stack<>();

    public Pool(Pool.Factory<E> factory) {
        this.factory = factory;
    }
    
    public int size() {
        return pool.size();
    }
    
    public boolean isEmpty() {
        return pool.isEmpty();
    }
    
    public E obtain() {
        return pool.isEmpty() ? factory._new() : pool.pop();
    }
    
    public void free(E e) {
        factory.reset(e);
        pool.push(Objects.requireNonNull(e));
    }
    
    public interface Factory<E> {
        E _new();
        default void reset(E e) {} // empty by default
    }
    
}
