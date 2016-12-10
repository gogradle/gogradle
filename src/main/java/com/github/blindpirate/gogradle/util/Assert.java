package com.github.blindpirate.gogradle.util;

import org.apache.http.util.Asserts;

public class Assert {
    public static void isTrue(boolean value) {
        Asserts.check(value, "This value cannot be false!");
    }

    public static void isTrue(boolean value, String s) {
        Asserts.check(value, s);
    }

    public static Object isNotNull(Object obj, String s) {
        isTrue(obj != null, s);
        return obj;
    }

    public static Object isNotNull(Object obj) {
        return isNotNull(obj, "This object cannot be null!");
    }

    public static <T> void exactInstanceOf(T instance, Class<? extends T> clazz) {
        isTrue(instance.getClass() == clazz);
    }
}
