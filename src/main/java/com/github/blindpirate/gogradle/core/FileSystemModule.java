package com.github.blindpirate.gogradle.core;

import com.github.blindpirate.gogradle.core.dependency.DefaultDependencyResolutionStrategy;
import com.github.blindpirate.gogradle.core.dependency.DependencyHelper;
import com.github.blindpirate.gogradle.core.dependency.resolve.DependencyResolutionStrategy;
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet;
import com.github.blindpirate.gogradle.core.dependency.GolangDependency;
import com.github.blindpirate.gogradle.core.pack.AbstractPakcageModule;
import org.gradle.api.artifacts.Dependency;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A {@link FileSystemModule} is a directory in file system,
 * regardless of stable or transient
 */
public abstract class FileSystemModule extends AbstractPakcageModule implements GolangDependency {

    private Path rootDir;

    private long updateTime;

    private GolangDependencySet dependencies;

    private DependencyResolutionStrategy dependencyResolutionStrategy
            = new DefaultDependencyResolutionStrategy();

    // Interface method in Dependency. Do not implement them temporarily.
    @Override
    public String getGroup() {
        return null;
    }

    @Override
    public String getVersion() {
        return null;
    }

    @Override
    public boolean contentEquals(Dependency dependency) {
        return false;
    }

    @Override
    public Dependency copy() {
        return null;
    }

    public abstract FileSystemModule vendor(Path relativePathToVendor);

    protected void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public FileSystemModule(String name, Path rootDir) {
        super(name);
        this.rootDir = rootDir;
    }

    protected Path addVendorPrefix(Path relativePathToVendor) {
        String relativeToParentModule = "vendor" + File.separator + relativePathToVendor;
        return Paths.get(relativeToParentModule);
    }

    public GolangDependencySet getDependencies() {
        if (dependencies == null) {
            dependencies =
                    DependencyHelper.resolveDirectDependencies(dependencyResolutionStrategy, this);
        }
        return dependencies;
    }

    @Override
    public Path getRootDir() {
        return rootDir;
    }

    @Override
    public long getUpdateTime() {
        return updateTime;
    }

    @Override
    public GolangPackageModule getPackage() {
        return this;
    }

}
