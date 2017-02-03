package com.github.blindpirate.gogradle.util;


public class Assert {
    public static void isTrue(boolean value) {
        isTrue(value, "This value cannot be false!");
    }

    public static void isTrue(boolean value, String s) {
        if (!value) {
            throw new IllegalStateException(s);
        }
    }

    public static <T> T isNotNull(T obj, String s) {
        isTrue(obj != null, s);
        return obj;
    }

    public static <T> T isNotNull(T obj) {
        return isNotNull(obj, "This object cannot be null!");
    }

    public static String isNotBlank(String s) {
        isTrue(StringUtils.isNotBlank(s));
        return s;
    }

    public static String isNotBlank(String s, String message) {
        isTrue(StringUtils.isNotBlank(s), message);
        return s;
    }
}
