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

package com.github.blindpirate.gogradle.core.dependency.produce.strategy;

import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet;
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency;
import com.github.blindpirate.gogradle.core.dependency.produce.DependencyVisitor;

import java.io.File;

/**
 * Direct how to generate dependencies of an existing golang package module.
 */
// Default: if external exist, use it; else if vendor exist, use it; else scan source

public interface DependencyProduceStrategy {

    DependencyProduceStrategy DEFAULT_STRATEGY = new DefaultDependencyProduceStrategy();

    GolangDependencySet produce(ResolvedDependency dependency,
                                File rootDir,
                                DependencyVisitor visitor,
                                String configurationName);
}
