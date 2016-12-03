package com.github.blindpirate.gogradle.core;

import java.nio.file.Path;

/**
 * Read git metadata to determine the updateTime
 */
public class GitTempModule extends FileSystemModule {
    public GitTempModule(String name, Path rootDir) {
        super(name, rootDir);
    }

    @Override
    public FileSystemModule vendor(Path relativePathToVendor) {
        return null;
    }
}
