package com.github.blindpirate.gogradle.core.pack;

import com.github.blindpirate.gogradle.core.GolangPackageModule;
import com.github.blindpirate.gogradle.core.FileSystemModule;

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

    private FileSystemModule parentModule;

    public VendorResolveContext(GolangPackageModule parentModuleOfVendorDirectory) {
        this.parentModule = (FileSystemModule) parentModuleOfVendorDirectory;
    }

    public void setCurrentPath(Path currentPath) {
        this.currentPath = currentPath;
    }

    public Path getCurrentPath() {
        return currentPath;
    }

    public FileSystemModule getParentModule() {
        return parentModule;
    }
}
