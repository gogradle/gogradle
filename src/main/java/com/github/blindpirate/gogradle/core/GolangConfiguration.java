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
import com.github.blindpirate.gogradle.core.dependency.DefaultDependencyRegistry;
import com.github.blindpirate.gogradle.core.dependency.DependencyRegistry;
import com.github.blindpirate.gogradle.core.dependency.GolangDependency;
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet;
import com.github.blindpirate.gogradle.core.dependency.parse.NotationParser;
import com.github.blindpirate.gogradle.core.pack.PackagePathResolver;
import groovy.lang.Closure;
import org.apache.commons.lang3.tuple.Pair;
import org.gradle.util.ConfigureUtil;

import java.util.ArrayList;
import java.util.List;

public class GolangConfiguration {
    public static final String BUILD = "build";
    public static final String TEST = "test";

    private final String name;
    private final GolangDependencySet dependencies = new GolangDependencySet();
    private final DependencyRegistry dependencyRegistry;
    private final List<Pair<Object, Closure>> firstLevelDependencies = new ArrayList<>();
    private final NotationParser<Object> notationParser;

    public GolangConfiguration(String name, NotationParser notationParser, PackagePathResolver packagePathResolver) {
        this.name = name;
        this.notationParser = notationParser;
        this.dependencyRegistry = new DefaultDependencyRegistry(packagePathResolver);
    }

    public DependencyRegistry getDependencyRegistry() {
        return dependencyRegistry;
    }

    public GolangDependencySet getDependencies() {
        return dependencies;
    }

    public String getName() {
        return name;
    }

    public void addFirstLevelDependency(Object notation, Closure closure) {
        firstLevelDependencies.add(Pair.of(notation, closure));
    }

    public GolangDependency create(Object dependencyNotation, Closure configureClosure) {
        // first level
        GolangDependency dependency = notationParser.parse(dependencyNotation);
        AbstractGolangDependency.class.cast(dependency).setFirstLevel(true);
        return ConfigureUtil.configure(configureClosure, dependency);
    }

    public void resolveFirstLevelDependencies() {
        firstLevelDependencies.forEach(pair -> dependencies.add(create(pair.getLeft(), pair.getRight())));
    }

    public boolean hasFirstLevelDependencies() {
        return !firstLevelDependencies.isEmpty();
    }
}
