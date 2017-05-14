/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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
