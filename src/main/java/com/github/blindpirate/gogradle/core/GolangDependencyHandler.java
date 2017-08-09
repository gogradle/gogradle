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

import com.github.blindpirate.gogradle.core.dependency.AbstractGolangDependency;
import com.github.blindpirate.gogradle.core.dependency.GolangDependency;
import com.github.blindpirate.gogradle.core.dependency.parse.NotationParser;
import groovy.lang.Closure;
import groovy.lang.GroovyObjectSupport;
import groovy.lang.MissingMethodException;
import org.apache.commons.lang3.tuple.Triple;
import org.gradle.util.CollectionUtils;
import org.gradle.util.ConfigureUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.tuple.Triple.of;

@Singleton
public class GolangDependencyHandler extends GroovyObjectSupport {
    private final NotationParser notationParser;
    private final GolangConfigurationManager configurationManager;
    private final List<Triple<GolangConfiguration, Object, Closure>> firstLevelDependencies = new ArrayList<>();

    @Inject
    public GolangDependencyHandler(GolangConfigurationManager configurationManager,
                                   NotationParser notationParser) {
        this.configurationManager = configurationManager;
        this.notationParser = notationParser;
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
        firstLevelDependencies.add(of(configuration, dependencyNotation, configureClosure));
    }

    public GolangDependency create(Object dependencyNotation, Closure configureClosure) {
        // first level
        GolangDependency dependency = notationParser.parse(dependencyNotation);
        AbstractGolangDependency.class.cast(dependency).setFirstLevel(true);
        return ConfigureUtil.configure(configureClosure, dependency);
    }

    public void resolveFirstLevel(GolangConfiguration configuration) {
        firstLevelDependencies.stream()
                .filter(triple -> configuration.equals(triple.getLeft()))
                .forEach(triple -> configuration.getDependencies().add(create(triple.getMiddle(), triple.getRight())));
    }

    public boolean hasFirstLevelDependencies() {
        return !firstLevelDependencies.isEmpty();
    }
}

