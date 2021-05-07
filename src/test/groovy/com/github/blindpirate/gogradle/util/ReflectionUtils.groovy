/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.github.blindpirate.gogradle.util

import org.joor.Reflect
import org.powermock.reflect.Whitebox

import java.lang.reflect.Field
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.lang.reflect.Modifier

class ReflectionUtils {
    static void setField(Object object, String field, Object value) {
        Reflect.on(object).set(field, value)
    }

    static void setFieldSafely(Object instance, String fieldName, Object value) {
        try {
            setField(instance, fieldName, value)
        } catch (Throwable e) {
            // ignore
        }
    }

    static Object getField(Object target, String field) {
        return Reflect.on(target).get(field)
    }

    // WARNING: do not set a static final field after getting it first
    // it will cause issues
    static void setStaticFinalField(Class targetClass, String fieldName, Object value) {
        Whitebox.setInternalState(targetClass, fieldName, value);
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

    static Object callStaticMethod(Class clazz, String methodName, Object... args) {
        Method method = clazz.getDeclaredMethod(methodName, args.collect { it.class } as Class[])
        method.invoke(null, args)
    }

    static void testUnsupportedMethods(Object targetInstance, Class clazzToTest, List<String> exclusionNames) {
        clazzToTest.methods.each {
            if (it.getDeclaringClass() == Object) {
                return
            }
            if (it.isDefault()) {
                return
            }
            if (['equals', 'hashCode'].contains(it.name)) {
                return
            }
            if (exclusionNames.contains(it.name)) {
                return
            }
            try {
                Object[] params = it.getParameterTypes().collect { clazz ->
                    if (clazz == boolean.class) {
                        return false
                    } else if (clazz.isPrimitive()) {
                        return 0
                    } else {
                        return null
                    }
                }
                it.invoke(targetInstance, params)
                println(it)
                assert false
            } catch (InvocationTargetException e) {
                if (!(e.cause instanceof UnsupportedOperationException)) {
                    println(it)
                }
                assert e.cause instanceof UnsupportedOperationException
            }
        }
    }
}
