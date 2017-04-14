package com.github.blindpirate.gogradle.core.cache;

import com.github.blindpirate.gogradle.core.GolangCloneable;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

public abstract class AbstractCache<K, V extends GolangCloneable> implements Cache<K, V> {
    protected ConcurrentMap<K, V> container = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public V get(K key, Function<K, V> constructor) {
        V ret = container.get(key);
        if (ret == null) {
            ret = constructor.apply(key);
            container.put(key, (V) ret.clone());
            return ret;
        } else {
            return (V) ret.clone();
        }
    }
}
