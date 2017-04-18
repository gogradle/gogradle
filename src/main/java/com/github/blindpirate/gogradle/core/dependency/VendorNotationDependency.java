package com.github.blindpirate.gogradle.core.dependency;

import com.github.blindpirate.gogradle.core.cache.CacheScope;
import com.github.blindpirate.gogradle.core.exceptions.DependencyResolutionException;

import java.util.Objects;
import java.util.Optional;

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
    public ResolvedDependency doResolve(ResolveContext context) {
        ResolvedDependency hostResolvedDependency = hostNotationDependency.resolve(context);
        Optional<VendorResolvedDependency> result = hostResolvedDependency.getDependencies().flatten()
                .stream()
                .filter(d -> d instanceof VendorResolvedDependency)
                .map(d -> (VendorResolvedDependency) d)
                .filter(d -> d.getRelativePathToHost().equals(vendorPath))
                .findFirst();

        if (result.isPresent()) {
            return result.get();
        } else {
            throw DependencyResolutionException.vendorNotExist(this, hostResolvedDependency);
        }
    }

    @Override
    public boolean equals(Object o) {
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
    public CacheScope getCacheScope() {
        return hostNotationDependency.getCacheScope();
    }
}
