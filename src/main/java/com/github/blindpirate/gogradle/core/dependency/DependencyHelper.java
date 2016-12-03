package com.github.blindpirate.gogradle.core.dependency;

import com.github.blindpirate.gogradle.core.GolangPackageModule;
import com.github.blindpirate.gogradle.core.dependency.vendor.VendorDependencyFactory;

import static com.github.blindpirate.gogradle.core.dependency.DependencyResolutionStrategy.DependencyType.Vendor;

public class DependencyHelper {
    private static VendorDependencyFactory vendorDependencyFactory = new VendorDependencyFactory();

    public static GolangDependencySet resolveDirectDependencies(DependencyResolutionStrategy strategy,
                                                                GolangPackageModule module) {
        GolangDependencySet vendorDependencies = resolveVendor(strategy, module);
        return vendorDependencies;
    }

    private static GolangDependencySet resolveVendor(DependencyResolutionStrategy strategy,
                                                     GolangPackageModule module) {
        if (!strategy.required(module, Vendor)) {
            return GolangDependencySet.emptySet();
        }
        return vendorDependencyFactory.produce(module);
    }
}
