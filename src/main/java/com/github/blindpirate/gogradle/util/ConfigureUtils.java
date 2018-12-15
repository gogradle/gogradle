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

package com.github.blindpirate.gogradle.util;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import groovy.lang.Closure;
import org.gradle.util.ConfigureUtil;
import org.joor.ReflectException;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static org.joor.Reflect.on;

@SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_INFERRED")
public class ConfigureUtils {
    public static <T> T configureByMapQuietly(Map<?, ?> properties, T delegate) {
        if (properties.isEmpty()) {
            return delegate;
        }

        properties.forEach((key, value) -> {
            String setter = "set" + property(key.toString());
            if (hasSetter(delegate, setter)) {
                on(delegate).call(setter, value);
            } else {
                try {
                    on(delegate).set(key.toString(), value);
                } catch (Exception ignore) {
                }
            }
        });

        return delegate;
    }

    private static <T> boolean hasSetter(T target, String setterName) {
        List<Method> methods = new ArrayList<>();

        Class clazz = target.getClass();

        do {
            methods.addAll(Arrays.asList(clazz.getDeclaredMethods()));
            clazz = clazz.getSuperclass();
        }
        while (clazz != Object.class);

        return methods.stream()
                .anyMatch(method -> method.getName().equals(setterName) && method.getParameterCount() == 1);
    }

    public static boolean match(Map<String, Object> properties, Object target) {
        return properties.entrySet().stream()
                .allMatch((entry) -> entryMatch(entry, target));
    }

    private static boolean entryMatch(Map.Entry<String, Object> propertyEntry, Object target) {
        String name = propertyEntry.getKey();
        Object value = propertyEntry.getValue();
        Optional<Object> result = getPojoValue(target, name);
        return result.filter(o -> Objects.equals(value, o)).isPresent();
    }

    private static Optional<Object> getPojoValue(Object target, String name) {
        try {
            return Optional.of(on(target).call("get" + property(name)).get());
        } catch (Exception e) {
            try {
                return Optional.of(on(target).call("is" + property(name)).get());
            } catch (ReflectException e1) {
                if (ExceptionHandler.getRootCause(e1) instanceof NoSuchMethodException) {
                    return Optional.empty();
                } else {
                    throw e1;
                }
            }
        }
    }

    private static String property(String string) {
        int length = string.length();

        if (length == 0) {
            return "";
        } else if (length == 1) {
            return string.toUpperCase();
        } else {
            return string.substring(0, 1).toUpperCase() + string.substring(1);
        }
    }

    public static void configure(Object target, Closure<?> configureClosure) {
        ConfigureUtil.configure(configureClosure, target);
    }
}
