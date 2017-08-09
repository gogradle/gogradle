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

package com.github.blindpirate.gogradle.core;

import groovy.lang.Closure;
import groovy.lang.GroovyObjectSupport;
import groovy.lang.MissingMethodException;
import org.gradle.util.CollectionUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

@Singleton
public class GolangDependencyHandler extends GroovyObjectSupport {
    private final GolangConfigurationManager configurationManager;

    @Inject
    public GolangDependencyHandler(GolangConfigurationManager configurationManager) {
        this.configurationManager = configurationManager;
    }

    public Object methodMissing(String name, Object args) {
        Object[] argsArray = (Object[]) args;
        GolangConfiguration configuration = configurationManager.getByName(name);
        if (configuration == null) {
            throw new MissingMethodException(name, this.getClass(), argsArray);
        }

        List<?> normalizedArgs = CollectionUtils.flattenCollections(argsArray);
        if (normalizedArgs.size() == 2 && normalizedArgs.get(1) instanceof Closure) {
            doAdd(configuration, normalizedArgs.get(0), (Closure) normalizedArgs.get(1));
        } else if (normalizedArgs.size() == 1) {
            doAdd(configuration, normalizedArgs.get(0), null);
        } else {
            for (Object arg : normalizedArgs) {
                doAdd(configuration, arg, null);
            }
        }
        return null;
    }

    private void doAdd(GolangConfiguration configuration, Object dependencyNotation, Closure configureClosure) {
        configuration.addFirstLevelDependency(dependencyNotation, configureClosure);
    }
}

