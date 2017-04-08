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
 * In {@code DEVELOP} mode, dependencies in build.gradle have top priority.
 * In {@code REPRODUCIBLE} mode, dependencies in vendor (or settings.gradle) have top priority.
 * <p>
 * Additionally, if there aren't any dependencies in build.gradle,
 * a scan for external dependency management tools will be performed.
 */
@Singleton
public class GogradleRootProduceStrategy implements DependencyProduceStrategy {

    private final GolangPluginSetting settings;
    private final GolangConfigurationManager configurationManager;
    private final LockedDependencyManager lockedDependenciesManager;


    @Inject
    public GogradleRootProduceStrategy(GolangPluginSetting settings,
                                       GolangConfigurationManager configurationManager,
                                       LockedDependencyManager lockedDependenciesManager) {
        this.settings = settings;
        this.configurationManager = configurationManager;
        this.lockedDependenciesManager = lockedDependenciesManager;
    }

    @DebugLog
    @Override
    public GolangDependencySet produce(ResolvedDependency dependency,
                                       File rootDir,
                                       DependencyVisitor visitor,
                                       String configuration) {
        // Here we can just fetch them from internal container
        GolangDependencySet declaredDependencies = getDependenciesInBuildDotGradle(configuration);
        GolangDependencySet lockedDependencies = getLockedDependencies(dependency, rootDir, visitor, configuration);
        GolangDependencySet vendorDependencies = visitor.visitVendorDependencies(dependency, rootDir);


        GolangDependencySet result = settings
                .getBuildMode()
                .determine(declaredDependencies,
                        vendorDependencies,
                        lockedDependencies);

        setFirstLevel(result);

        if (result.isEmpty()) {
            return visitor.visitSourceCodeDependencies(dependency, rootDir, configuration);
        } else {
            return result;
        }
    }

    private void setFirstLevel(GolangDependencySet set) {
        set.forEach(dependency -> AbstractGolangDependency.class.cast(dependency).setFirstLevel(true));
    }


    // locked by gogradle or external tools
    private GolangDependencySet getLockedDependencies(ResolvedDependency dependency,
                                                      File rootDir,
                                                      DependencyVisitor visitor,
                                                      String configuration) {
        GolangDependencySet lockedByGogradle = lockedDependenciesManager.getLockedDependencies(configuration);
        if (lockedByGogradle.isEmpty()) {
            return visitor.visitExternalDependencies(dependency, rootDir, configuration);
        } else {
            return lockedByGogradle;
        }
    }

    private GolangDependencySet getDependenciesInBuildDotGradle(String configuration) {
        return configurationManager.getByName(configuration).getDependencies();
    }
}
