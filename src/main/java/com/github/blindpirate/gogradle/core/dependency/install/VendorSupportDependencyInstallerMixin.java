package com.github.blindpirate.gogradle.core.dependency.install;

import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency;
import com.github.blindpirate.gogradle.core.dependency.VendorResolvedDependency;

public interface VendorSupportDependencyInstallerMixin extends DependencyInstaller {
    default ResolvedDependency determineDependency(ResolvedDependency dependency) {
        if (dependency instanceof VendorResolvedDependency) {
            return VendorResolvedDependency.class.cast(dependency).getHostDependency();
        } else {
            return dependency;
        }
    }

    default String determineRelativePath(ResolvedDependency dependency) {
        if (dependency instanceof VendorResolvedDependency) {
            return VendorResolvedDependency.class.cast(dependency).getRelativePathToHost();
        } else {
            return ".";
        }
    }
}
