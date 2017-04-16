package com.github.blindpirate.gogradle.core.dependency;

import com.github.blindpirate.gogradle.core.dependency.resolve.DependencyResolver;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        VendorNotationDependency that = (VendorNotationDependency) o;
        return Objects.equals(hostNotationDependency, that.hostNotationDependency)
                && Objects.equals(vendorPath, that.vendorPath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), hostNotationDependency, vendorPath);
    }

    @Override
    public boolean isConcrete() {
        return hostNotationDependency.isConcrete();
    }
}
