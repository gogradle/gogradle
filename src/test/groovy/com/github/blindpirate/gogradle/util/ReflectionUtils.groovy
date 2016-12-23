package com.github.blindpirate.gogradle.util

import java.lang.reflect.Field
import java.lang.reflect.Modifier

public class ReflectionUtils {
    public static void setField(Object object, String field, Object value) {
        org.gradle.internal.impldep.org.codehaus.plexus.util.ReflectionUtils
                .setVariableValueInObject(object, field, value);
    }

    public static Object getField(Object target, String field) {
        return org.gradle.internal.impldep.org.codehaus.plexus.util.ReflectionUtils
                .getValueIncludingSuperclasses(field, target);
    }

    public static void setStaticFinalField(Object object, String fieldName, Object value) {
        Field field = org.gradle.internal.impldep.org.codehaus.plexus.util.ReflectionUtils
                .getFieldByNameIncludingSuperclasses(fieldName, object.getClass());

        field.setAccessible(true)
        Field modifiersField = Field.class.getDeclaredField('modifiers')
        modifiersField.setAccessible(true)
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL)
        field.set(object, value)
    }
}
