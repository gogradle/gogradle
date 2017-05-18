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

import com.github.blindpirate.gogradle.GolangPluginSetting;
import com.github.blindpirate.gogradle.core.GolangConfigurationManager;
import com.github.blindpirate.gogradle.core.dependency.AbstractGolangDependency;
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet;
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency;
import com.github.blindpirate.gogradle.core.dependency.lock.LockedDependencyManager;
import com.github.blindpirate.gogradle.core.dependency.produce.DependencyVisitor;
import com.github.blindpirate.gogradle.util.logging.DebugLog;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;

/**
 * In {@code DEVELOP} mode, dependencies in build.gradle have higher priority.
 * In {@code REPRODUCIBLE} mode, dependencies in gogradle.lock have higher priority.
 * <p>
 * Additionally, if there aren't any dependencies in build.gradle,
 * a scan for external dependency management tools will be performed.
 */
@Singleton
public class GogradleRootProduceStrategy implements DependencyProduceStrategy {

    private final GolangPluginSetting settings;
    private final GolangConfigurationManager configurationManager;
    private final LockedDependencyManager lockedDependencyManager;

    @Inject
    public GogradleRootProduceStrategy(GolangPluginSetting settings,
                                       GolangConfigurationManager configurationManager,
                                       LockedDependencyManager lockedDependencyManager) {
        this.settings = settings;
        this.configurationManager = configurationManager;
        this.lockedDependencyManager = lockedDependencyManager;
    }

    @DebugLog
    @Override
    public GolangDependencySet produce(ResolvedDependency dependency,
                                       File projectRoot,
                                       DependencyVisitor visitor,
                                       String configuration) {
        GolangDependencySet result = determineDependencies(projectRoot, configuration);

        setFirstLevel(result);

        if (result.isEmpty()) {
            return visitor.visitSourceCodeDependencies(dependency, projectRoot, configuration);
        } else {
            return result;
        }
    }

    private GolangDependencySet determineDependencies(File projectRoot, String configuration) {
        // Here we can just fetch them from internal container
        GolangDependencySet declaredDependencies = getDependenciesInBuildDotGradle(configuration);
        if (lockedDependencyManager.canRecognize(projectRoot)) {
            // gogradle.lock exists
            GolangDependencySet lockedDependencies = lockedDependencyManager.produce(projectRoot, configuration);
            return settings.getBuildMode().determine(declaredDependencies, lockedDependencies);
        } else {
            return declaredDependencies;
        }
    }

    private void setFirstLevel(GolangDependencySet set) {
        set.forEach(dependency -> AbstractGolangDependency.class.cast(dependency).setFirstLevel(true));
    }

    private GolangDependencySet getDependenciesInBuildDotGradle(String configuration) {
        return configurationManager.getByName(configuration).getDependencies();
    }
}
