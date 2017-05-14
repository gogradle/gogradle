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

package com.github.blindpirate.gogradle.core.dependency.resolve;

import com.github.blindpirate.gogradle.core.cache.ProjectCacheManager;
import com.github.blindpirate.gogradle.core.dependency.NotationDependency;
import com.github.blindpirate.gogradle.core.dependency.ResolveContext;
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.util.concurrent.atomic.AtomicBoolean;

public interface CacheEnabledDependencyResolverMixin extends DependencyManager {
    Logger LOGGER = Logging.getLogger(CacheEnabledDependencyResolverMixin.class);

    default ResolvedDependency resolve(ResolveContext context, NotationDependency dependency) {
        AtomicBoolean functionInvoked = new AtomicBoolean(false);

        ResolvedDependency ret = getProjectCacheManager()
                .resolve(dependency, notationDependency -> {
                    functionInvoked.set(true);
                    return doResolve(context, notationDependency);
                });

        if (functionInvoked.get()) {
            LOGGER.quiet("Resolving {}", dependency);
        } else {
            LOGGER.quiet("Resolving cached {}", dependency);
        }

        return ret;
    }

    ProjectCacheManager getProjectCacheManager();

    ResolvedDependency doResolve(ResolveContext context, NotationDependency dependency);
}
