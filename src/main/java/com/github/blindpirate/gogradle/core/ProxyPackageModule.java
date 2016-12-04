package com.github.blindpirate.gogradle.core;

import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet;
import com.github.blindpirate.gogradle.core.pack.AbstractPakcageModule;

import java.nio.file.Path;

/**
 * A {@link ProxyPackageModule} is a proxy that represent a package that may be resolved later.
 * <p>
 * When resolved, it will record all information of underlying package module.
 */
public class ProxyPackageModule extends AbstractPakcageModule {
    private GolangDependencySet dependencies;

    private long updateTime;

    public ProxyPackageModule(GolangPackageModule delegate) {
        super(delegate.getName());
        this.dependencies = delegate.getDependencies();
        this.updateTime = delegate.getUpdateTime();
    }


    public GolangDependencySet getDependencies() {
        return dependencies;
    }

    @Override
    public Path getRootDir() {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getUpdateTime() {
        return updateTime;
    }
}
