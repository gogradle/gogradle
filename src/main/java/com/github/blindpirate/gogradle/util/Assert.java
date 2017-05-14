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


import java.util.Collection;

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

    public static <T> void isNotEmpty(Collection<T> collection, String message) {
        isTrue(!collection.isEmpty(), message);
    }
}
