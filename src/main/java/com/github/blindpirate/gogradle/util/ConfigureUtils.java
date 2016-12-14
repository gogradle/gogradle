package com.github.blindpirate.gogradle.util;

import org.gradle.api.internal.DynamicObjectUtil;
import org.gradle.internal.metaobject.DynamicObject;
import org.gradle.internal.metaobject.InvokeMethodResult;
import org.gradle.internal.metaobject.SetPropertyResult;

import java.util.Map;

public class ConfigureUtils {
    public static <T> T configureByMapQuietly(Map<?, ?> properties, T delegate) {
        DynamicObject dynamicObject = DynamicObjectUtil.asDynamicObject(delegate);

        for (Map.Entry<?, ?> entry : properties.entrySet()) {
            String name = entry.getKey().toString();
            Object value = entry.getValue();

            SetPropertyResult setterResult = new SetPropertyResult();
            dynamicObject.setProperty(name, value, setterResult);
            if (setterResult.isFound()) {
                continue;
            }
            // if not found, skip it quietly
            dynamicObject.invokeMethod(name, new InvokeMethodResult(), value);
        }

        return delegate;
    }
}
