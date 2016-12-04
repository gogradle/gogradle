package com.github.blindpirate.gogradle.util;

public class ReflectionUtils {
    public static void setField(Object object, String field, Object value) {
        org.gradle.internal.impldep.org.codehaus.plexus.util.ReflectionUtils
                .setVariableValueInObject(object, field, value);
    }
}
