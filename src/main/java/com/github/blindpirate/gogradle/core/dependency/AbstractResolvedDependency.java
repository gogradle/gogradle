package com.github.blindpirate.gogradle.core.dependency;

import com.github.blindpirate.gogradle.core.cache.CacheScope;
import com.github.blindpirate.gogradle.core.dependency.resolve.DependencyManager;
import com.github.blindpirate.gogradle.util.Assert;
import com.github.blindpirate.gogradle.util.IOUtils;

import java.io.File;

import static com.github.blindpirate.gogradle.core.dependency.resolve.DependencyManager.CURRENT_VERSION_INDICATOR_FILE;

/**
 * Represents some code at a specific version.
 */
public abstract class AbstractResolvedDependency extends AbstractGolangDependency implements ResolvedDependency {
    private String version;
    private long updateTime;

    private GolangDependencySet dependencies = GolangDependencySet.empty();

    protected AbstractResolvedDependency(String name, String version, long updateTime) {
        setName(name);

        this.version = version;
        this.updateTime = updateTime;
    }

    public void setDependencies(GolangDependencySet dependencies) {
        this.dependencies = dependencies;
    }

    @Override
    public ResolvedDependency resolve(ResolveContext context) {
        return this;
    }

    @Override
    public long getUpdateTime() {
        return updateTime;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public GolangDependencySet getDependencies() {
        return dependencies;
    }

    @Override
    public void installTo(File targetDirectory) {
        IOUtils.write(targetDirectory, CURRENT_VERSION_INDICATOR_FILE, formatVersion());
        getInstaller().install(this, targetDirectory);
    }

    protected abstract DependencyManager getInstaller();

    @Override
    public String toString() {
        return getName() + ":" + formatVersion();
    }

    @Override
    public CacheScope getCacheScope() {
        return CacheScope.PERSISTENCE;
    }

    @Override
    public Object clone() {
        AbstractResolvedDependency ret = (AbstractResolvedDependency) super.clone();
        Assert.isTrue(onlyVendorDependenciesCanHaveDescendants());
        ret.dependencies = this.dependencies.clone();
        if (getClass() != VendorResolvedDependency.class) {
            ret.dependencies.flatten().forEach(dependency -> resetVendorHostIfNecessary(dependency, ret));
        }
        return ret;
    }

    private boolean onlyVendorDependenciesCanHaveDescendants() {
        return this.dependencies.stream().
                filter(d -> (d instanceof ResolvedDependency) && !(d instanceof VendorResolvedDependency))
                .map(d -> (ResolvedDependency) d)
                .allMatch(d -> d.getDependencies().isEmpty());
    }

    private void resetVendorHostIfNecessary(GolangDependency dependency, ResolvedDependency clone) {
        if (dependency instanceof VendorResolvedDependency) {
            VendorResolvedDependency.class.cast(dependency).setHostDependency(clone);
        }
    }
}
