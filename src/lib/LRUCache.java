package lib;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * @author Texhnolyze
 * @param <K> key 
 * @param <V> value (heavy object) to be cached. Must implement LRUCache.Sizeable
 */
public interface LRUCache<K, V extends LRUCache.Sizeable> {
    
    interface Sizeable {
        long getSizeBytes();
    }

    @FunctionalInterface
    interface ByKeySizeableLoader<K, V extends Sizeable> {
        V load(K key);
    }
    
    long getSizeBytes();
    long getMaxCapacityBytes();
    void setMaxCapacity(long capacityBytes);
    
    V getIfPresent(K key);
    V getIfPresentOrLoad(K key, ByKeySizeableLoader<K, ? extends V> loader);
    Iterable<Map.Entry<K, V>> fromLeastToMostRecentlyUsed();
    
    static <K, V extends Sizeable> LRUCache<K, V> defaultImpl(long capacityBytes) {
        return new LRUCacheImpl<>(capacityBytes);
    }
    
    class LRUCacheImpl<K, V extends Sizeable> implements LRUCache<K, V> {

        private long sizeBytes;
        private long capacityBytes;
        private final LinkedHashMap<K, V> map = new LinkedHashMap<>();
        
        LRUCacheImpl(long capacityBytes) {
            this.capacityBytes = capacityBytes;
        }
        
        @Override public long getSizeBytes() {return sizeBytes;}
        @Override public long getMaxCapacityBytes() {return capacityBytes;}

        @Override
        public void setMaxCapacity(long capacityBytes) {
            if (capacityBytes <= 0)
                throw new IllegalArgumentException("Capacity must be greater than 0.");
            this.capacityBytes = capacityBytes;
            Iterator<Entry<K, V>> it = map.entrySet().iterator();
            while (sizeBytes > capacityBytes) {
                V v = it.next().getValue();
                sizeBytes -= v.getSizeBytes();
                it.remove();
            }
        }

        @Override
        public V getIfPresent(K key) {
            V val = map.get(key);
            if (val != null) {
                map.remove(key);
                map.put(key, val);
            }
            return val;
        }
        
        @Override
        public V getIfPresentOrLoad(K key, ByKeySizeableLoader<K, ? extends V> loader) {
            V val = map.get(key);
            if (val != null) {
                map.remove(key);
                map.put(key, val);
            } else {
                val = loader.load(key);
                if (val == null)
                    return null;
                long size = val.getSizeBytes();
                if (capacityBytes < size)
                    throw new RuntimeException("Insufficient cache space.");
                Iterator<Entry<K, V>> it = map.entrySet().iterator();
                while (sizeBytes + size > capacityBytes) {
                    V v = it.next().getValue();
                    sizeBytes -= v.getSizeBytes();
                    it.remove();
                }
                sizeBytes += val.getSizeBytes();
                map.put(key, val);
            }                
            return val;
        }
        
        @Override
        public Iterable<Map.Entry<K, V>> fromLeastToMostRecentlyUsed() {
            return Collections.unmodifiableSet(map.entrySet());
        }
        
    }
    
}
