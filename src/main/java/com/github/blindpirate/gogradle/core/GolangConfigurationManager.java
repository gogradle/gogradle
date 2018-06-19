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

import com.github.blindpirate.gogradle.core.dependency.parse.NotationParser;
import com.github.blindpirate.gogradle.core.pack.PackagePathResolver;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

import static com.github.blindpirate.gogradle.core.GolangConfiguration.BUILD;
import static com.github.blindpirate.gogradle.core.GolangConfiguration.TEST;
import static com.github.blindpirate.gogradle.core.pack.DefaultPackagePathResolver.AllPackagePathResolvers;

@Singleton
public class GolangConfigurationManager {
    private final Map<String, GolangConfiguration> configurations = new HashMap<>();

    @Inject
    public GolangConfigurationManager(NotationParser notationParser,
                                      @AllPackagePathResolvers PackagePathResolver packagePathResolver) {
        configurations.put(BUILD, new GolangConfiguration(BUILD, notationParser, packagePathResolver));
        configurations.put(TEST, new GolangConfiguration(TEST, notationParser, packagePathResolver));
    }

    public GolangConfiguration getByName(String name) {
        return configurations.get(name);
    }
}
