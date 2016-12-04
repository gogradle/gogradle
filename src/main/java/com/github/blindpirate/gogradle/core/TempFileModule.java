package com.github.blindpirate.gogradle.core;

import java.nio.file.Path;

/**
 * Read git metadata to determine the updateTime
 */
public class TempFileModule extends FileSystemModule {
    public TempFileModule(String name, Path rootDir) {
        super(name, rootDir);
    }

    @Override
    public GolangPackageModule vendor(Path relativePathToVendor) {
        return null;
    }
}
