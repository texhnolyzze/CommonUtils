package lib;

import java.util.Objects;

/**
 *
 * @author Texhnolyze
 */
public abstract class Pool<E> {
    
    public abstract int size();
    public boolean isEmpty() {return size() == 0;}
    
    public abstract E obtain();
    public abstract void free(E e);
    
    public static interface Factory<E> {
        E _new();
    }
    
    public static interface Reseter<E> {
        void reset(E e);
    }
    
    public static <E> Pool<E> newUnlimitedPool(Factory<E> f, Reseter<E> r) {
        return new DefaultPool<>(f, r);
    }
    
    public static <E> Pool<E> newFixedPool(Factory<E> f, Reseter<E> r, int maxPoolSize) {
        return new DefaultPool<>(f, r, maxPoolSize);
    }
    
    private static class DefaultPool<E> extends Pool<E> {
        
        private final Factory<E> factory;
        private final Reseter<E> reseter;
        private final Stack<E> pool = new Stack<>();
        
        private final boolean limited;
        private final int maxPoolSize;
        private int createdNewObjects;
        
        private DefaultPool(Factory<E> factory, Reseter<E> reseter) {
            this(factory, reseter, -1);
        }

        private DefaultPool(Factory<E> factory, Reseter<E> reseter, int maxPoolSize) {
            this.factory = factory;
            this.reseter = reseter;
            this.limited = maxPoolSize != -1;
            this.maxPoolSize = maxPoolSize;
        }

        @Override
        public int size() {
            return pool.size();
        }

        @Override
        public E obtain() {
            if (limited && pool.isEmpty() && ++createdNewObjects > maxPoolSize)
                throw new IllegalStateException();
            return pool.isEmpty() ? factory._new() : pool.pop();
        }

        @Override
        public void free(E e) {
            reseter.reset(Objects.requireNonNull(e));
            pool.push(e);
        }
        
    }
    
}
