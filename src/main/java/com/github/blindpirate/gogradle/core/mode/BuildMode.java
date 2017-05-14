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

package com.github.blindpirate.gogradle.core.mode;

import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet;
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency;

import static com.github.blindpirate.gogradle.core.dependency.GolangDependencySet.merge;

public enum BuildMode {
    DEVELOP {
        @Override
        public GolangDependencySet determine(GolangDependencySet declaredDependencies,
                                             GolangDependencySet vendorDependencies,
                                             GolangDependencySet lockedDependencies) {
            GolangDependencySet declaredAndLocked = merge(declaredDependencies, lockedDependencies);

            vendorDependencies.flatten()
                    .stream()
                    .map(dependency -> (ResolvedDependency) dependency)
                    .forEach(dependency -> dependency.getDependencies().removeAll(declaredAndLocked));

            return merge(declaredAndLocked, vendorDependencies);
        }
    },
    REPRODUCIBLE {
        @Override
        public GolangDependencySet determine(GolangDependencySet declaredDependencies,
                                             GolangDependencySet vendorDependencies,
                                             GolangDependencySet lockedDependencies) {
            GolangDependencySet lockedAndDeclared = merge(lockedDependencies, declaredDependencies);

            lockedAndDeclared.removeAll(vendorDependencies.flatten());

            return merge(vendorDependencies, lockedAndDeclared);
        }
    };

    public abstract GolangDependencySet determine(
            GolangDependencySet declaredDependencies,
            GolangDependencySet vendorDependencies,
            GolangDependencySet lockedDependencies);
}
