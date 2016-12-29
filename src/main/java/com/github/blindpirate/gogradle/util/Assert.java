package com.github.blindpirate.gogradle.util;

import org.apache.http.util.Asserts;

import java.util.Collection;

public class Assert {
    public static void isTrue(boolean value) {
        Asserts.check(value, "This value cannot be false!");
    }

    public static void isTrue(boolean value, String s) {
        Asserts.check(value, s);
    }

    public static <T> T isNotNull(T obj, String s) {
        isTrue(obj != null, s);
        return obj;
    }

    public static <T> T isNotNull(T obj) {
        return isNotNull(obj, "This object cannot be null!");
    }

    public static <T> void isNotEmpty(Collection<T> identityFiles, String message) {
        isTrue(!identityFiles.isEmpty(), message);
    }

    public static String isNotBlank(String name) {
        isTrue(StringUtils.isNotBlank(name));
        return name;
    }
}
