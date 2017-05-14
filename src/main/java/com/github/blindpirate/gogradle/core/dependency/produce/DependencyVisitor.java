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

package com.github.blindpirate.gogradle.core.dependency.produce;

import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet;
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency;

import java.io.File;

public interface DependencyVisitor {

    /**
     * Visits dependencies managed by a external package management tool such as godep, govendor, etc.
     * Usually, it's determined by analyzing "lock file" of that tool.
     *
     * @param dependency    The dependency to visit
     * @param rootDir       Root directory of a package
     * @param configuration Current context, {@code GolangConfiguration.BUILD} or {@code GolangConfiguration.TEST}
     * @return Dependencies managed by external package management tools
     */
    GolangDependencySet visitExternalDependencies(ResolvedDependency dependency,
                                                  File rootDir,
                                                  String configuration);

    /**
     * Visits dependencies in vendor.
     *
     * @param dependency    The dependency to visit
     * @param rootDir       Root directory of a package
     * @param configuration Current context, {@code GolangConfiguration.BUILD} or {@code GolangConfiguration.TEST}
     * @return Dependencies in vendor directory
     */
    GolangDependencySet visitVendorDependencies(ResolvedDependency dependency,
                                                File rootDir,
                                                String configuration);

    /**
     * Analyze the imports in source code (all .go files in root directory except vendor) to get dependencies.
     *
     * @param dependency    The dependency to visit
     * @param rootDir       Root directory of a package
     * @param configuration Current context, build or test
     * @return All imported package.
     */
    GolangDependencySet visitSourceCodeDependencies(ResolvedDependency dependency,
                                                    File rootDir,
                                                    String configuration);
}
