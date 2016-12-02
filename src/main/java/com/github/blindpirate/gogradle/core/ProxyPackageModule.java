package com.github.blindpirate.gogradle.core;

import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet;
import com.github.blindpirate.gogradle.core.pack.AbstractPakcageModule;

import java.nio.file.Path;
import java.util.Date;

/**
 * A {@link ProxyPackageModule} is a proxy that represent a package that may be resolved later.
 */
public class ProxyPackageModule extends AbstractPakcageModule {

    private GolangPackageModule delegate;

    public ProxyPackageModule(String name, GolangPackageModule delegate) {
        super(name);
        this.delegate = delegate;
    }

    public GolangDependencySet getDependencies() {
        return delegate.getDependencies();
    }

    @Override
    public Path getRootDir() {
        return delegate.getRootDir();
    }

    @Override
    public Date getUpdateTime() {
        return delegate.getUpdateTime();
    }
}
