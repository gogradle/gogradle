package com.github.blindpirate.gogradle.core.cache;

import java.util.function.Function;

public interface Cache<K, V> {
    V get(K key, Function<K, V> constructor);
}
