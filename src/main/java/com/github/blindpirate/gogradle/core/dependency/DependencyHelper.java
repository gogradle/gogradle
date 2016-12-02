package com.github.blindpirate.gogradle.core.dependency;

import com.github.blindpirate.gogradle.core.GolangPackageModule;
import com.github.blindpirate.gogradle.core.dependency.vendor.VendorDependencyFactory;
import com.google.common.base.Optional;

public class DependencyHelper {
    private static VendorDependencyFactory vendorDependencyFactory;

    public static GolangDependencySet resolveFirstLevelDependencies(DependencyResolutionStrategy strategy,
                                                                    GolangPackageModule module) {
        Optional<GolangDependencySet> vendorDependencies = resolveVendor(strategy, module);
        return null;
    }

    private static Optional<GolangDependencySet> resolveVendor(DependencyResolutionStrategy strategy, GolangPackageModule module) {
        if (!strategy.needVendorDependencies(module)) {
            return Optional.absent();
        }
        return Optional.of(vendorDependencyFactory.produce(module));
    }
}
