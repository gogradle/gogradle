package com.github.blindpirate.gogradle.core.cache;

import com.github.blindpirate.gogradle.core.GolangCloneable;
import org.apache.commons.collections4.map.LRUMap;

import java.util.Map;
import java.util.function.Function;

// NOT THREAD-SAFE
public abstract class AbstractCache<K, V extends GolangCloneable> implements Cache<K, V> {
    protected static final int CAPACITY = 1000;
    protected Map<K, V> container = new LRUMap<>(CAPACITY);

    @SuppressWarnings("unchecked")
    public V get(K key, Function<K, V> constructor) {
        V cachedItem = container.get(key);
        if (cachedItem == null) {
            V ret = constructor.apply(key);
            cachedItem = (V) ret.clone();
            container.put(key, cachedItem);
            return ret;
        } else {
            return (V) cachedItem.clone();
        }
    }
}
