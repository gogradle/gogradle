package com.github.blindpirate.gogradle.core.dependency.resolve;

import com.github.blindpirate.gogradle.core.dependency.NotationDependency;
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency;
import com.github.blindpirate.gogradle.core.dependency.VendorNotationDependency;
import com.github.blindpirate.gogradle.core.dependency.VendorResolvedDependency;

import javax.inject.Singleton;

@Singleton
public class VendorDependencyResolver implements DependencyResolver {
    @Override
    public VendorResolvedDependency resolve(NotationDependency dependency) {
        VendorNotationDependency vendorNotationDependency = (VendorNotationDependency) dependency;
        ResolvedDependency resolvedHostDependency = vendorNotationDependency.getHostNotationDependency().resolve();

        return VendorResolvedDependency.fromHost(dependency.getName(),
                resolvedHostDependency,
                vendorNotationDependency.getVendorPath());
    }
}
