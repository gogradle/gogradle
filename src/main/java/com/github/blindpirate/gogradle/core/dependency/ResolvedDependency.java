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

package com.github.blindpirate.gogradle.core.dependency;

import java.io.File;
import java.util.Map;

public interface ResolvedDependency extends GolangDependency {

    /**
     * The update time of a dependency. It will be used in resolving package conflict.
     * Generally speaking, package with newest update time will win.
     *
     * @return the update time determined by the package. It may be the last modified time
     * of a file on filesystem or in scm.
     */
    long getUpdateTime();

    /**
     * Get transitive dependencies of this package.
     *
     * @return the transitive dependencies
     */
    GolangDependencySet getDependencies();

    Map<String, Object> toLockedNotation();

    void installTo(File targetDirectory);

    String formatVersion();

}
