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
        if (properties.isEmpty()) {
            return false;
        }
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
