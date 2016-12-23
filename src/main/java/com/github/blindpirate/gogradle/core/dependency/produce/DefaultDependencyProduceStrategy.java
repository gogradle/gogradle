package com.github.blindpirate.gogradle.core.dependency.produce;

import com.github.blindpirate.gogradle.core.GolangPackageModule;
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet;
import com.github.blindpirate.gogradle.core.dependency.resolve.ModuleDependencyVistor;
import com.github.blindpirate.gogradle.util.logging.DebugLog;
import com.google.common.base.Optional;

import javax.inject.Singleton;
import java.util.List;

import static com.github.blindpirate.gogradle.util.CollectionUtils.collectOptional;

/**
 * Default strategy to generate dependencies of a module.
 * <p>
 * First, it will check if there are external package manager and vendor director.
 * If so, use them and let vendor dependencies have higher priority.
 * Otherwise, as a fallback, it will scan source code to get dependencies.
 */
@Singleton
public class DefaultDependencyProduceStrategy implements DependencyProduceStrategy {
    @Override
    @DebugLog
    public GolangDependencySet produce(GolangPackageModule module, ModuleDependencyVistor vistor) {
        Optional<GolangDependencySet> externalDependencies = vistor.visitExternalDependencies(module);

        Optional<GolangDependencySet> vendorDependencies = vistor.visitVendorDependencies(module);

        List<GolangDependencySet> candicates = collectOptional(vendorDependencies, externalDependencies);

        if (!candicates.isEmpty()) {
            return GolangDependencySet.merge(candicates);
        }

        return vistor.visitSourceCodeDependencies(module);
    }
}
