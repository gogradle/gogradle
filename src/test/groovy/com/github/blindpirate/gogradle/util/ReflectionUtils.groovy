package com.github.blindpirate.gogradle.util

import java.lang.reflect.Field
import java.lang.reflect.Modifier

class ReflectionUtils {
    static void setField(Object object, String field, Object value) {
        org.gradle.internal.impldep.org.codehaus.plexus.util.ReflectionUtils
                .setVariableValueInObject(object, field, value)
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
                .getValueIncludingSuperclasses(field, target)
    }

    // WARNING: do not set a static final field after getting it first
    // it will cause issues
    static void setStaticFinalField(Class targetClass, String fieldName, Object value) {
        Field field = org.gradle.internal.impldep.org.codehaus.plexus.util.ReflectionUtils
                .getFieldByNameIncludingSuperclasses(fieldName, targetClass)
        field.setAccessible(true)
        Field modifiersField = Field.class.getDeclaredField('modifiers')
        modifiersField.setAccessible(true)
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL)
        field.set(null, value)
    }

    static Object getStaticField(Class target, String fieldName) {
        Field field = org.gradle.internal.impldep.org.codehaus.plexus.util.ReflectionUtils
                .getFieldByNameIncludingSuperclasses(fieldName, target)

        // Field.get() has side effect since it will cache FieldAccessor in overrideFieldAccessor
        field.setAccessible(true)
        Object result = field.get(null)
        return result
    }

    static boolean allFieldsEquals(Object actual, Object expected, List<String> fieldNames) {
        return fieldNames.every { actual[it] == expected[it] }
    }
}
