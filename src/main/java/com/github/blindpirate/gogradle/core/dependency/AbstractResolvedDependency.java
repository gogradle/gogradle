package com.github.blindpirate.gogradle.core.dependency;

import com.github.blindpirate.gogradle.GogradleGlobal;
import com.github.blindpirate.gogradle.core.GolangConfiguration;
import com.github.blindpirate.gogradle.core.dependency.install.DependencyInstaller;

import java.io.File;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Represents some code at a specific version.
 */
public abstract class AbstractResolvedDependency extends AbstractGolangDependency implements ResolvedDependency {
    private String version;
    private long updateTime;

    protected AbstractResolvedDependency(String name, String version, long updateTime) {
        setName(name);
        this.version = version;
        this.updateTime = updateTime;
    }

    private GolangDependencySet dependencies = GolangDependencySet.empty();

    public void setDependencies(GolangDependencySet dependencies) {
        this.dependencies = dependencies;
    }

    @Override
    public ResolvedDependency resolve(GolangConfiguration configuration) {
        return this;
    }

    @Override
    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public GolangDependencySet getDependencies() {
        return dependencies
                .stream()
                .filter(super::shouldNotBeExcluded)
                .collect(Collectors.toCollection(GolangDependencySet::new));
    }

    @Override
    public void installTo(File targetDirectory) {
        GogradleGlobal.getInstance(getInstallerClass()).install(this, targetDirectory);
    }

    protected abstract Class<? extends DependencyInstaller> getInstallerClass();

    @Override
    public String toString() {
        return getName() + ":" + formatVersion();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AbstractResolvedDependency that = (AbstractResolvedDependency) o;
        return Objects.equals(version, that.version)
                && Objects.equals(getName(), that.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(version, getName());
    }
}
