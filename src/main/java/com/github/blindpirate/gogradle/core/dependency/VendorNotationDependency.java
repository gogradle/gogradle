package com.github.blindpirate.gogradle.core.dependency;

import com.github.blindpirate.gogradle.core.dependency.resolve.DependencyResolver;

public class VendorNotationDependency extends AbstractNotationDependency {

    private NotationDependency hostNotationDependency;

    private String vendorPath;

    public String getVendorPath() {
        return vendorPath;
    }

    public NotationDependency getHostNotationDependency() {
        return hostNotationDependency;
    }

    public void setHostNotationDependency(NotationDependency hostNotationDependency) {
        this.hostNotationDependency = hostNotationDependency;
    }

    public void setVendorPath(String vendorPath) {
        this.vendorPath = vendorPath;
    }

    @Override
    public Class<? extends DependencyResolver> getResolverClass() {
        return AbstractNotationDependency.class.cast(hostNotationDependency).getResolverClass();
    }
}
