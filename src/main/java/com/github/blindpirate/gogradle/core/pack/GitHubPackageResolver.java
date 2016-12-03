package com.github.blindpirate.gogradle.core.pack;

import com.github.blindpirate.gogradle.core.FileSystemModule;

import java.nio.file.Path;

public class GitHubPackageResolver implements PackageNameResolver {
    private static final String GITHUB_HOST = "github.com";

    @Override
    public VendorPackageNameResolveResult produce(VendorResolveContext context) {

        // github.com/a is not a dependency unit
        // github.com/a/b is a dependency unit
        if (context.getCurrentPath().getNameCount() <= 2) {
            return VendorPackageNameResolveResult.toBeCountinued();
        } else if (context.getCurrentPath().getNameCount() == 3) {
            return createByDirectory(context);
        }

        return null;
    }

    private VendorPackageNameResolveResult createByDirectory(VendorResolveContext context) {
        FileSystemModule module = context.getParentModule()
                .vendor(context.getCurrentPath());
        return VendorPackageNameResolveResult.of(module);
    }

    @Override
    public boolean accept(VendorResolveContext vendorResolveContext) {
        Path packagePath = vendorResolveContext.getCurrentPath();
        return GITHUB_HOST.equals(packagePath.getName(0).toString());
    }
}
