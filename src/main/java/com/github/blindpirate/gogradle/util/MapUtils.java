package com.github.blindpirate.gogradle.util;

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
}
