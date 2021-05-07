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

package com.github.blindpirate.gogradle.util;

import com.google.common.collect.Maps;

import java.util.HashMap;
import java.util.Map;

public class MapUtils {
    public static String getString(Map<String, Object> map, String key) {
        return getString(map, key, null);
    }

    public static String getString(Map<String, Object> map, String key, String defaultValue) {
        if (map != null) {
            Object answer = map.get(key);
            if (answer != null) {
                return answer.toString();
            }
        }
        return defaultValue;
    }

    @SuppressWarnings("unchecked")
    public static <T> T getValue(Map<String, Object> map, String key, Class<T> clazz) {
        return (T) map.get(key);
    }

    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> asMap(K k1, V v1, K k2, V v2) {
        return asMap(new Map.Entry[]{Maps.immutableEntry(k1, v1), Maps.immutableEntry(k2, v2)});
    }

    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> asMap(K k1, V v1, K k2, V v2, K k3, V v3) {
        return asMap(Maps.immutableEntry(k1, v1), Maps.immutableEntry(k2, v2), Maps.immutableEntry(k3, v3));
    }

    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> asMap(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
        return asMap(new Map.Entry[]{Maps.immutableEntry(k1, v1), Maps.immutableEntry(k2, v2), Maps.immutableEntry(k3, v3), Maps.immutableEntry(k4, v4)});
    }

    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> asMap(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5) {
        return asMap(Maps.immutableEntry(k1, v1), Maps.immutableEntry(k2, v2), Maps.immutableEntry(k3, v3), Maps.immutableEntry(k4, v4), Maps.immutableEntry(k5, v5));
    }

    private static <K, V> Map<K, V> asMap(Map.Entry<K, V>... entries) {
        Map<K, V> ret = new HashMap<>();
        for (Map.Entry<K, V> entry : entries) {
            ret.put(entry.getKey(), entry.getValue());
        }
        return ret;
    }

    public static <K, V> Map<K, V> asMapWithoutNull(K k, V v) {
        return asMapWithoutNull(Maps.immutableEntry(k, v));
    }

    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> asMapWithoutNull(K k1, V v1, K k2, V v2, K k3, V v3) {
        return asMapWithoutNull(Maps.immutableEntry(k1, v1), Maps.immutableEntry(k2, v2), Maps.immutableEntry(k3, v3));
    }

    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> asMapWithoutNull(K k1, V v1, K k2, V v2) {
        return asMapWithoutNull(new Map.Entry[]{Maps.immutableEntry(k1, v1), Maps.immutableEntry(k2, v2)});
    }

    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> asMapWithoutNull(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
        return asMapWithoutNull(new Map.Entry[]{Maps.immutableEntry(k1, v1), Maps.immutableEntry(k2, v2), Maps.immutableEntry(k3, v3), Maps.immutableEntry(k4, v4)});
    }

    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> asMapWithoutNull(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5) {
        return asMapWithoutNull(Maps.immutableEntry(k1, v1), Maps.immutableEntry(k2, v2), Maps.immutableEntry(k3, v3), Maps.immutableEntry(k4, v4), Maps.immutableEntry(k5, v5));
    }

    private static <K, V> Map<K, V> asMapWithoutNull(Map.Entry<K, V>... entries) {
        Map<K, V> ret = new HashMap<>();
        for (Map.Entry<K, V> entry : entries) {
            if (entry.getKey() != null && entry.getValue() != null) {
                ret.put(entry.getKey(), entry.getValue());
            }
        }
        return ret;
    }

    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> asMap(K k, V v) {
        return asMapWithoutNull(Maps.immutableEntry(k, v));
    }
}
