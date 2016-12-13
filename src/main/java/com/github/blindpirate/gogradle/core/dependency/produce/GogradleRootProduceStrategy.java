package com.github.blindpirate.gogradle.core.dependency.produce;

import com.github.blindpirate.gogradle.GolangPluginSetting;
import com.github.blindpirate.gogradle.core.GolangPackageModule;
import com.github.blindpirate.gogradle.core.dependency.GolangConfiguration;
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet;
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet.DependencySetFacade;
import com.github.blindpirate.gogradle.core.dependency.LockedDependencyManager;
import com.github.blindpirate.gogradle.core.dependency.resolve.ModuleDependencyVistor;
import com.google.common.base.Optional;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.internal.Cast;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

import static com.github.blindpirate.gogradle.GolangPlugin.BUILD_CONFIGURATION_NAME;
import static com.github.blindpirate.gogradle.core.mode.BuildMode.Develop;
import static com.github.blindpirate.gogradle.util.CollectionUtils.collectOptional;

/**
 * In {@code Develop} mode, dependencies in build.gradle have top priority.
 * In {@code Reproducible} mode, dependencies in vendor (or setting.gradle) have top priority
 */
@Singleton
public class GogradleRootProduceStrategy implements DependencyProduceStrategy {

    @Inject
    private GolangPluginSetting settings;
    @Inject
    private ConfigurationContainer configurationContainer;
    @Inject
    private LockedDependencyManager lockedDependenciesManager;

    @Override
    public GolangDependencySet produce(GolangPackageModule module, ModuleDependencyVistor vistor) {

        // Here we can just fetch them from internal container
        GolangDependencySet dependenciesInBuildDotGradle = getDependenciesInBuildDotGradle();

        Optional<GolangDependencySet> lockedDependencies = getLockedDependencies();
        Optional<GolangDependencySet> vendorDependencies = vistor.visitVendorDependencies(module);

        List<GolangDependencySet> candidates = new ArrayList<>();

        if (settings.getBuildMode() == Develop) {
            candidates.add(dependenciesInBuildDotGradle);
            candidates.addAll(collectOptional(vendorDependencies, lockedDependencies));
        } else {
            candidates.addAll(collectOptional(vendorDependencies, lockedDependencies));
            candidates.add(dependenciesInBuildDotGradle);
        }

        GolangDependencySet result = GolangDependencySet.merge(candidates);

        if (result.isEmpty()) {
            return vistor.visitSourceCodeDependencies(module);
        } else {
            return result;
        }
    }


    private Optional<GolangDependencySet> getLockedDependencies() {
        return lockedDependenciesManager.getLockedDependencies();
    }

    private GolangDependencySet getDependenciesInBuildDotGradle() {
        GolangConfiguration configuration =
                (GolangConfiguration) configurationContainer.getByName(BUILD_CONFIGURATION_NAME);

        return Cast.cast(DependencySetFacade.class, configuration.getDependencies()).toGolangDependencies();
    }

}
