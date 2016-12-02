package com.github.blindpirate.gogradle.core.pack;

import com.github.blindpirate.gogradle.core.GolangPackageModule;
import com.github.blindpirate.gogradle.core.FileSystemPackageModule;

import java.nio.file.Path;

/**
 * Provides some information in process of resolving packages in vendor directory.
 * <p>
 * A new instance will be created when resolving a recursive vendor directory.
 */
public class VendorResolveContext {

    /**
     * current relative path to the vendor directory
     */
    private Path currentPath;

    private FileSystemPackageModule parentModule;

    public VendorResolveContext(GolangPackageModule parentModuleOfVendorDirectory) {
        this.parentModule = (FileSystemPackageModule) parentModuleOfVendorDirectory;
    }

    public void setCurrentPath(Path currentPath) {
        this.currentPath = currentPath;
    }

    public Path getCurrentPath() {
        return currentPath;
    }

    public FileSystemPackageModule getParentModule() {
        return parentModule;
    }
}
