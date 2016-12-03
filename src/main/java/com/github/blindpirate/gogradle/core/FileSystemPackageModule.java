package com.github.blindpirate.gogradle.core;

import com.github.blindpirate.gogradle.core.dependency.DefaultDependencyResolutionStrategy;
import com.github.blindpirate.gogradle.core.dependency.DependencyResolutionStrategy;
import com.github.blindpirate.gogradle.core.pack.AbstractPakcageModule;
import com.github.blindpirate.gogradle.core.dependency.DependencyHelper;
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet;
import com.github.blindpirate.gogradle.core.dependency.GolangPackageDependency;

import java.nio.file.Path;
import java.util.Date;

/**
 * A {@link FileSystemPackageModule} is a directory in file system,
 * regardless of stable or transient
 */
public abstract class FileSystemPackageModule extends AbstractPakcageModule implements GolangPackageDependency {

    private Path rootDir;

    protected Date updateTime;

    private DependencyResolutionStrategy dependencyResolutionStrategy
            = new DefaultDependencyResolutionStrategy();

    public abstract FileSystemPackageModule vendor(Path relativePathToVendor);

    public FileSystemPackageModule(String name, Path rootDir) {
        super(name);
        this.rootDir = rootDir;
    }

    public GolangDependencySet getDependencies() {
        return DependencyHelper.resolveFirstLevelDependencies(dependencyResolutionStrategy, this);
    }

    @Override
    public Path getRootDir() {
        return rootDir;
    }

    @Override
    public Date getUpdateTime() {
        return updateTime;
    }

    @Override
    public GolangPackageModule getPackage() {
        return this;
    }

}
