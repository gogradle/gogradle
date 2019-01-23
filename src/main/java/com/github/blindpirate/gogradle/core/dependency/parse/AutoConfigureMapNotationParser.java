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

package com.github.blindpirate.gogradle.core.dependency.parse;

import com.github.blindpirate.gogradle.core.dependency.NotationDependency;
import com.github.blindpirate.gogradle.util.ConfigureUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

public abstract class AutoConfigureMapNotationParser<T extends NotationDependency> implements MapNotationParser {
    @Override
    public NotationDependency parse(Map<String, Object> notationMap) {
        try {
            preConfigure(notationMap);
            NotationDependency ret = determineDependencyClass(notationMap).getConstructor().newInstance();
            ConfigureUtils.configureByMapQuietly(notationMap, ret);

            postConfigure(notationMap, ret);
            return ret;
        } catch (Exception e) {
            throw new IllegalStateException("Dependency class must have an accessible default constructor!", e);
        }
    }

    protected void preConfigure(Map<String, Object> notationMap) {
    }

    protected void postConfigure(Map<String, Object> notationMap, NotationDependency ret) {
    }

    @SuppressWarnings("unchecked")
    protected Class<? extends NotationDependency> determineDependencyClass(Map<String, Object> notationMap) {
        Type superClass = getClass().getGenericSuperclass();
        return (Class<T>) ((ParameterizedType) superClass).getActualTypeArguments()[0];
    }
}
