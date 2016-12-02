package com.github.blindpirate.gogradle.core.pack;

import com.github.blindpirate.gogradle.core.FileSystemPackageModule;
import com.github.blindpirate.gogradle.core.dependency.GolangPackageDependency;

public class VendorPackageNameResolveResult {

    private FileSystemPackageModule module;

    public boolean isFinished() {
        return module == null;
    }

    public GolangPackageDependency getDependency() {
        return module;
    }

    public static VendorPackageNameResolveResult toBeCountinued() {
        return new VendorPackageNameResolveResult();
    }

    public static VendorPackageNameResolveResult of(FileSystemPackageModule module) {
        VendorPackageNameResolveResult ret = new VendorPackageNameResolveResult();
        ret.module = module;
        return ret;
    }
}
