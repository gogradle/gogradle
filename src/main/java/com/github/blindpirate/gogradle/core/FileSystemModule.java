package com.github.blindpirate.gogradle.core;

import com.github.blindpirate.gogradle.core.dependency.GolangDependency;
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet;
import com.github.blindpirate.gogradle.core.pack.AbstractPackageModule;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A {@link FileSystemModule} is a directory in file system,
 * regardless of stable or transient
 */
public abstract class FileSystemModule extends AbstractPackageModule implements GolangDependency {

    private Path rootDir;

    private long updateTime;

    private GolangDependencySet dependencies;

    public abstract FileSystemModule vendor(String packageName);

    public FileSystemModule(String name, Path rootDir, long updateTime) {
        super(name);
        this.rootDir = rootDir;
        this.updateTime = updateTime;
    }

    protected Path addVendorPrefix(String relativePathToVendor) {
        String relativeToParentModule = "vendor" + File.separator + relativePathToVendor;
        return Paths.get(relativeToParentModule);
    }

    public GolangDependencySet getDependencies() {
        if (dependencies == null) {
            dependencies = InjectionHelper.produceDependencies(this).get();
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
