package my_lib;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * @author Texhnolyze
 * @param <K> key 
 * @param <V> value (heavy object to be cached)
 */
public interface LRUCache<K, V> {
    
    int getMaxSize();
    void setMaxSize(int maxSize);
    
    V getFromCache(K key);
    Iterable<Map.Entry<K, V>> fromLeastToMostRecentlyUsed();
    
    public interface ValueByKeyGenerator<K, V> {
        V next(K key);
    }
    
    public static <K, V> LRUCache<K, V> defaultImpl(ValueByKeyGenerator<K, V> gen, int maxSize) {
        return new LRUCacheImpl<>(gen, maxSize);
    }
    
    public static class LRUCacheImpl<K, V> extends LinkedHashMap<K, V> implements LRUCache<K, V> {
    
        private final ValueByKeyGenerator<K, V> gen;

        private int maxSize;

        private LRUCacheImpl(ValueByKeyGenerator<K, V> gen, int maxSize) {
            super(maxSize);
            this.gen = gen;
            this.maxSize = maxSize;
        }

        @Override public int getMaxSize() {return maxSize;}
        @Override public void setMaxSize(int maxSize) {this.maxSize = maxSize;}

        @Override
        protected boolean removeEldestEntry(Entry<K, V> eldest) {
            return size() == maxSize + 1;
        }

        @Override
        public V getFromCache(K key) {
            V v = get(key);
            if (v == null) {
                v = gen.next(key);
                put(key, v);
            } else {
                remove(key);
                put(key, v);
            }
            return v;
        }

        @Override
        public Iterable<Entry<K, V>> fromLeastToMostRecentlyUsed() {
            return this.entrySet();
        }
        
    }
    
}
