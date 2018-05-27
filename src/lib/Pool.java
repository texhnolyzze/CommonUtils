package lib;

import java.util.Objects;

/**
 *
 * @author Texhnolyze
 */
public abstract class Pool<E> {
    
    private final Stack<E> pool = new Stack<>();
    
    protected abstract E _new();
    
    public int size() {
        return pool.size();
    }
    
    public boolean isEmpty() {
        return pool.isEmpty();
    }
    
    public E obtain() {
        return pool.isEmpty() ? _new() : pool.pop();
    }
    
    public void free(E e) {
        pool.push(Objects.requireNonNull(e));
    }
    
}
