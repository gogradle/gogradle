package com.github.blindpirate.gogradle.core.pack;

import com.github.blindpirate.gogradle.core.FileSystemModule;
import com.github.blindpirate.gogradle.core.dependency.GolangDependency;

public class VendorPackageNameResolveResult {

    private FileSystemModule module;

    public boolean isFinished() {
        return module != null;
    }

    public GolangDependency getDependency() {
        return module;
    }

    public static VendorPackageNameResolveResult toBeCountinued() {
        return new VendorPackageNameResolveResult();
    }

    public static VendorPackageNameResolveResult of(FileSystemModule module) {
        VendorPackageNameResolveResult ret = new VendorPackageNameResolveResult();
        ret.module = module;
        return ret;
    }
}
