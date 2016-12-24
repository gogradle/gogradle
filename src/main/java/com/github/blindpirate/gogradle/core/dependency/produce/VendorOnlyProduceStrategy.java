package com.github.blindpirate.gogradle.core.dependency.produce;

import com.github.blindpirate.gogradle.core.GolangPackageModule;
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet;
import com.github.blindpirate.gogradle.core.dependency.resolve.ModuleDependencyVistor;
import com.github.blindpirate.gogradle.util.logging.DebugLog;
import java.util.Optional;

import javax.inject.Singleton;

@Singleton
public class VendorOnlyProduceStrategy implements DependencyProduceStrategy {
    @Override
    @DebugLog
    public GolangDependencySet produce(GolangPackageModule module, ModuleDependencyVistor vistor) {
        Optional<GolangDependencySet> result = vistor.visitVendorDependencies(module);
        if (result.isPresent()) {
            return result.get();
        } else {
            return new GolangDependencySet();
        }
    }
}
