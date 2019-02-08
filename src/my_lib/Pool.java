package my_lib;

/**
 *
 * @author Texhnolyze
 */
public abstract class Pool<E> {
    
    public abstract int size();
    public boolean isEmpty() {return size() == 0;}
    public abstract int numObjectsCreated();
    
    public abstract E obtain();
    public abstract void free(E e);
    
    
    public static interface Factory<E> {
        E _new();
    }
    
    public static interface Reseter<E> {
        void reset(E e);
    }
	
    public static <E> Builder<E> builder(Factory<E> f) {
        return new Pool.Builder<>(f);
    }
	
    public static class Builder<E> {

        private int maxSize = -1;
        private Pool.Reseter<E> reseter;

        private final Pool.Factory<E> factory;

        private Builder(Pool.Factory<E> factory) {this.factory = factory;}

        //if maxSize < 0 then the number of objects created is unlimited
        public Builder<E> setObjectsCanCreate(int maxSize) {this.maxSize = maxSize; return this;}
        public Builder<E> setReseter(Reseter<E> reseter) {this.reseter = reseter; return this;}

        public Pool<E> build() {
            return new PoolImpl<>(factory, reseter, maxSize);
        }

    }
    
    private static class PoolImpl<E> extends Pool<E> {
        
        private final Factory<E> factory;
        private final Reseter<E> reseter;
        private final Stack<E> pool = new Stack<>();
        
        private final int maxPoolSize;
        private int numObjectsCreated;
        
        private PoolImpl(Factory<E> factory, Reseter<E> reseter, int maxPoolSize) {
            this.factory = factory;
            this.reseter = reseter;
            this.maxPoolSize = maxPoolSize;
        }

        @Override
        public int size() {
            return pool.size();
        }

        @Override
        public int numObjectsCreated() {
            return numObjectsCreated;
        }

        @Override
        public E obtain() {
            if (maxPoolSize >= 0 && pool.isEmpty() && numObjectsCreated + 1 > maxPoolSize)
                throw new IllegalStateException();
            E e;
            if (pool.isEmpty()) {
                e = factory._new();
                numObjectsCreated++;
            } else 
                e = pool.pop();
            return e;
        }

        @Override
        public void free(E e) {
            reseter.reset(e);
            pool.push(e);
        }
        
    }
    
}
