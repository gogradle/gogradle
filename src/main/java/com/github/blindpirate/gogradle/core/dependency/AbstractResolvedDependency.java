package com.github.blindpirate.gogradle.core.dependency;

import com.github.blindpirate.gogradle.core.GolangConfiguration;
import com.github.blindpirate.gogradle.core.dependency.install.DependencyInstaller;

import java.io.File;
import java.util.stream.Collectors;

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
    public ResolvedDependency resolve(GolangConfiguration configuration) {
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
        getInstaller().install(this, targetDirectory);
    }

    protected abstract DependencyInstaller getInstaller();

    @Override
    public String toString() {
        return getName() + ":" + formatVersion();
    }


}
