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

import static com.github.blindpirate.gogradle.core.GolangConfiguration.TEST;

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
                                       File rootDir,
                                       DependencyVisitor visitor,
                                       String configuration) {
        // Here we can just fetch them from internal container
        GolangDependencySet declaredDependencies = getDependenciesInBuildDotGradle(configuration);
        GolangDependencySet vendorDependencies = getVendorDependencies(visitor, dependency, rootDir, configuration);
        GolangDependencySet lockedDependencies = getDependenciesInGogradleDotLock(rootDir, configuration);

        GolangDependencySet result = settings.getBuildMode()
                .determine(declaredDependencies, vendorDependencies, lockedDependencies);

        setFirstLevel(result);

        if (result.isEmpty()) {
            return visitor.visitSourceCodeDependencies(dependency, rootDir, configuration);
        } else {
            return result;
        }
    }

    private GolangDependencySet getDependenciesInGogradleDotLock(File rootDir, String configuration) {
        if (lockedDependencyManager.canRecognize(rootDir)) {
            return lockedDependencyManager.produce(rootDir, configuration);
        } else {
            return GolangDependencySet.empty();
        }

    }

    private GolangDependencySet getVendorDependencies(DependencyVisitor visitor,
                                                      ResolvedDependency dependency,
                                                      File rootDir,
                                                      String configuration) {
        if (TEST.equals(configuration)) {
            return GolangDependencySet.empty();
        } else {
            return visitor.visitVendorDependencies(dependency, rootDir, configuration);
        }
    }

    private void setFirstLevel(GolangDependencySet set) {
        set.forEach(dependency -> AbstractGolangDependency.class.cast(dependency).setFirstLevel(true));
    }

    private GolangDependencySet getDependenciesInBuildDotGradle(String configuration) {
        return configurationManager.getByName(configuration).getDependencies();
    }
}
