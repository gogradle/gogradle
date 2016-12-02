package com.github.blindpirate.gogradle.core;

import java.io.File;

/**
 * A package in vendor directory of a package would be seen as
 * a {@link VendorPackageModule} which will participate in conflict resolution.
 */
public class VendorPackageModule extends StableFileSystemPackageModule {
    public VendorPackageModule(String name, File rootDir) {
        super(name, rootDir);
    }
}
