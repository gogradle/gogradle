package com.github.blindpirate.gogradle.core.dependency;

import com.github.blindpirate.gogradle.core.InjectionHelper;
import com.github.blindpirate.gogradle.core.dependency.install.DependencyInstaller;

import java.io.File;
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
    public ResolvedDependency resolve() {
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
        return dependencies
                .stream()
                .filter(super::shouldNotBeExcluded)
                .collect(Collectors.toCollection(GolangDependencySet::new));
    }

    @Override
    public void installTo(File targetDirectory) {
        InjectionHelper.INJECTOR_INSTANCE.getInstance(getInstallerClass()).install(this, targetDirectory);
    }

    protected abstract Class<? extends DependencyInstaller> getInstallerClass();

    @Override
    public String toString() {
        return getName() + ":" + formatVersion();
    }

}
