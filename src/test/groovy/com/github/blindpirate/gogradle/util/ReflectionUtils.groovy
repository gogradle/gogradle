package com.github.blindpirate.gogradle.util

import java.lang.reflect.Field
import java.lang.reflect.Modifier

class ReflectionUtils {
    static void setField(Object object, String field, Object value) {
        org.gradle.internal.impldep.org.codehaus.plexus.util.ReflectionUtils
                .setVariableValueInObject(object, field, value);
    }

    static void setFieldSafely(Object instance, String fieldName, Object value) {
        try {
            setField(instance, fieldName, value)
        } catch (Throwable e) {
            // ignore
        }
    }

    static Object getField(Object target, String field) {
        return org.gradle.internal.impldep.org.codehaus.plexus.util.ReflectionUtils
                .getValueIncludingSuperclasses(field, target);
    }

    static void setStaticFinalField(Object object, String fieldName, Object value) {
        Field field = org.gradle.internal.impldep.org.codehaus.plexus.util.ReflectionUtils
                .getFieldByNameIncludingSuperclasses(fieldName, object.getClass());

        field.setAccessible(true)
        Field modifiersField = Field.class.getDeclaredField('modifiers')
        modifiersField.setAccessible(true)
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL)
        field.set(object, value)
    }

    static Object getStaticField(Class target, String fieldName) {
        Field field = target.getField(fieldName)
        field.setAccessible(true)
        return field.get(null)
    }

    static boolean allFieldsEquals(Object actual, Object expected, List<String> fieldNames) {
        return fieldNames.every { actual[it] == expected[it] }
    }
}
