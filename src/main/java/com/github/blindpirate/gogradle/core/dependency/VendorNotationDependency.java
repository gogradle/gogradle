package com.github.blindpirate.gogradle.core.dependency;

import com.github.blindpirate.gogradle.core.dependency.resolve.DependencyResolver;

public class VendorNotationDependency extends AbstractNotationDependency {

    public static final String VENDOR_PATH_KEY = "vendorPath";

    private NotationDependency hostNotationDependency;

    private String vendorPath;

    public String getVendorPath() {
        return vendorPath;
    }

    public NotationDependency getHostDependency() {
        return hostNotationDependency;
    }

    public VendorNotationDependency(NotationDependency hostNotationDependency, String vendorPath) {
        this.hostNotationDependency = hostNotationDependency;
        this.vendorPath = vendorPath;
    }

    @Override
    public Class<? extends DependencyResolver> getResolverClass() {
        return AbstractNotationDependency.class.cast(hostNotationDependency).getResolverClass();
    }
}
