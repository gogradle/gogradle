package com.github.blindpirate.gogradle.core;

import java.nio.file.Path;

/**
 * Read git metadata to determine the updateTime
 */
public class GitTempPackageModule extends FileSystemPackageModule {
    public GitTempPackageModule(String name, Path rootDir) {
        super(name, rootDir);
    }

    @Override
    public FileSystemPackageModule vendor(Path relativePathToVendor) {
        return null;
    }
}
