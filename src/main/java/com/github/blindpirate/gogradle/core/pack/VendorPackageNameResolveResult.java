package com.github.blindpirate.gogradle.core.pack;

import com.github.blindpirate.gogradle.core.GolangPackageModule;
import com.github.blindpirate.gogradle.core.dependency.GolangDependency;

public class VendorPackageNameResolveResult {

    private GolangPackageModule module;

    public boolean isFinished() {
        return module != null;
    }

    public GolangDependency getDependency() {
        return module;
    }

    public static VendorPackageNameResolveResult toBeCountinued() {
        return new VendorPackageNameResolveResult();
    }

    public static VendorPackageNameResolveResult of(GolangPackageModule module) {
        VendorPackageNameResolveResult ret = new VendorPackageNameResolveResult();
        ret.module = module;
        return ret;
    }
}
