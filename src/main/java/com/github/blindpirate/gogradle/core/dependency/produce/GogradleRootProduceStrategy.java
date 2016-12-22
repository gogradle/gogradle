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

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

import static com.github.blindpirate.gogradle.GolangPlugin.BUILD_CONFIGURATION_NAME;
import static com.github.blindpirate.gogradle.core.mode.BuildMode.Develop;
import static com.github.blindpirate.gogradle.util.CollectionUtils.collectOptional;

/**
 * In {@code Develop} mode, dependencies in build.gradle have top priority.
 * In {@code Reproducible} mode, dependencies in vendor (or settings.gradle) have top priority.
 * <p>
 * Additionally, if there aren't any dependencies in build.gradle,
 * a scan for external dependency management tools will be performed.
 */
@Singleton
public class GogradleRootProduceStrategy implements DependencyProduceStrategy {

    private final GolangPluginSetting settings;
    private final ConfigurationContainer configurationContainer;
    private final LockedDependencyManager lockedDependenciesManager;


    public GogradleRootProduceStrategy(GolangPluginSetting settings,
                                       ConfigurationContainer configurationContainer,
                                       LockedDependencyManager lockedDependenciesManager) {
        this.settings = settings;
        this.configurationContainer = configurationContainer;
        this.lockedDependenciesManager = lockedDependenciesManager;
    }

    @Override

    public GolangDependencySet produce(GolangPackageModule module, ModuleDependencyVistor vistor) {

        // Here we can just fetch them from internal container
        GolangDependencySet declaredDependencies = getDependenciesInBuildDotGradleOrExternalTool(module, vistor);
        Optional<GolangDependencySet> lockedDependencies = getLockedDependencies();
        Optional<GolangDependencySet> vendorDependencies = vistor.visitVendorDependencies(module);

        List<GolangDependencySet> candidates = new ArrayList<>();

        if (settings.getBuildMode() == Develop) {
            candidates.add(declaredDependencies);
            candidates.addAll(collectOptional(vendorDependencies, lockedDependencies));
        } else {
            candidates.addAll(collectOptional(vendorDependencies, lockedDependencies));
            candidates.add(declaredDependencies);
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

    private GolangDependencySet getDependenciesInBuildDotGradleOrExternalTool(GolangPackageModule module,
                                                                              ModuleDependencyVistor vistor) {
        GolangConfiguration configuration =
                (GolangConfiguration) configurationContainer.getByName(BUILD_CONFIGURATION_NAME);

        GolangDependencySet dependenciesInBuildDotGradle
                = Cast.cast(DependencySetFacade.class, configuration.getDependencies()).toGolangDependencies();
        if (dependenciesInBuildDotGradle.isEmpty()) {
            Optional<GolangDependencySet> externalDependencies = vistor.visitExternalDependencies(module);
            if (externalDependencies.isPresent()) {
                return externalDependencies.get();
            }
        }
        return dependenciesInBuildDotGradle;
    }

}
