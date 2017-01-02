package com.github.blindpirate.gogradle.util;

import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.Map;

public class MapUtils {
    public static String getString(Map<String, Object> map, String key) {
        return org.gradle.internal.impldep.org.apache.commons.collections.MapUtils
                .getString(map, key);
    }

    public static boolean getBooleanValue(Map<String, Object> map, String key, boolean defaultValue) {
        return org.gradle.internal.impldep.org.apache.commons.collections.MapUtils
                .getBooleanValue(map, key, defaultValue);
    }


    public static <T> T getValue(Map<String, Object> map, String key, Class<T> clazz) {
        return (T) map.get(key);
    }

    public static <K, V> Map<K, V> asMap(K k1, V v1, K k2, V v2) {
        return asMap(new Pair[]{Pair.of(k1, v1), Pair.of(k2, v2)});
    }

    private static <K, V> Map<K, V> asMap(Pair<K, V>... entries) {
        Map<K, V> ret = new HashMap<>();
        for (Pair<K, V> entry : entries) {
            ret.put(entry.getLeft(), entry.getRight());
        }
        return ret;
    }

    public static <K, V> Map<K, V> asMapWithoutNull(K k, V v) {
        return asMapWithoutNull(Pair.of(k, v));
    }

    public static <K, V> Map<K, V> asMapWithoutNull(K k1, V v1, K k2, V v2) {
        return asMapWithoutNull(new Pair[]{Pair.of(k1, v1), Pair.of(k2, v2)});
    }

    public static <K, V> Map<K, V> asMapWithoutNull(K k1, V v1, K k2, V v2, K k3, V v3) {
        return asMapWithoutNull(Pair.of(k1, v1), Pair.of(k2, v2), Pair.of(k3, v3));
    }

    private static <K, V> Map<K, V> asMapWithoutNull(Pair<K, V>... entries) {
        Map<K, V> ret = new HashMap<>();
        for (Pair<K, V> entry : entries) {
            if (entry.getLeft() != null && entry.getRight() != null) {
                ret.put(entry.getLeft(), entry.getRight());
            }
        }
        return ret;
    }

    public static <K, V> Map<K, V> asMap(K k, V v) {
        return asMapWithoutNull(Pair.of(k, v));
    }
}
