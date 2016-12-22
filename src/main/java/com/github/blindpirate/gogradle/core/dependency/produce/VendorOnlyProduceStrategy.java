package com.github.blindpirate.gogradle.core.dependency.produce;

import com.github.blindpirate.gogradle.core.GolangPackageModule;
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet;
import com.github.blindpirate.gogradle.core.dependency.resolve.ModuleDependencyVistor;
import com.google.common.base.Optional;

import javax.inject.Singleton;

@Singleton
public class VendorOnlyProduceStrategy implements DependencyProduceStrategy {
    @Override
    public GolangDependencySet produce(GolangPackageModule module, ModuleDependencyVistor vistor) {
        Optional<GolangDependencySet> result = vistor.visitVendorDependencies(module);
        if (result.isPresent()) {
            return result.get();
        } else {
            return new GolangDependencySet();
        }
    }
}
