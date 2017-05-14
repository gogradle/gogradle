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

import org.gradle.api.internal.DynamicObjectUtil;
import org.gradle.internal.metaobject.DynamicObject;
import org.gradle.internal.metaobject.GetPropertyResult;
import org.gradle.internal.metaobject.SetPropertyResult;

import java.util.Map;
import java.util.Objects;

public class ConfigureUtils {
    public static <T> T configureByMapQuietly(Map<?, ?> properties, T delegate) {
        DynamicObject dynamicObject = DynamicObjectUtil.asDynamicObject(delegate);

        properties.entrySet().forEach(entry -> {
            String name = entry.getKey().toString();
            Object value = entry.getValue();

            SetPropertyResult setterResult = new SetPropertyResult();
            dynamicObject.setProperty(name, value, setterResult);
        });

        return delegate;
    }

    public static boolean match(Map<String, Object> properties, Object target) {
        DynamicObject dynamicObject = DynamicObjectUtil.asDynamicObject(target);
        return properties.entrySet().stream()
                .allMatch((entry) -> entryMatch(entry, dynamicObject));
    }

    private static boolean entryMatch(Map.Entry<String, Object> propertyEntry, DynamicObject dynamicObject) {
        String name = propertyEntry.getKey();
        Object value = propertyEntry.getValue();
        GetPropertyResult getPropertyResult = new GetPropertyResult();
        dynamicObject.getProperty(name, getPropertyResult);
        if (!getPropertyResult.isFound()) {
            return false;
        } else {
            return Objects.equals(getPropertyResult.getValue(), value);
        }
    }
}
