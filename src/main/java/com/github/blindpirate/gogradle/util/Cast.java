package com.github.blindpirate.gogradle.util;

public class Cast {
    public static <O, I> O cast(Class<O> outputType, I object) {
        return org.gradle.internal.Cast.cast(outputType, object);
    }

    public static <T> T uncheckedCast(Object object) {
        return org.gradle.internal.Cast.uncheckedCast(object);
    }
}
