package com.github.blindpirate.gogradle.core.dependency.resolve;

import com.github.blindpirate.gogradle.core.FileSystemModule;
import com.github.blindpirate.gogradle.core.GolangPackageModule;
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet;
import com.github.blindpirate.gogradle.core.pack.GitHubPackageResolver;
import com.github.blindpirate.gogradle.core.pack.PackageNameResolver;
import com.github.blindpirate.gogradle.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * A {@link VendorDependencyFactory is a factory that reads vendor directory and resolves them to
 * {@link FileSystemModule }
 */
public class VendorDependencyFactory implements DependencyFactory {
    private List<PackageNameResolver> resolvers = new ArrayList<>();

    {
        resolvers.add(new GitHubPackageResolver());
    }


    @Override
    public GolangDependencySet produce(GolangPackageModule module) {
        return resolveVendor(module);
    }

    private GolangDependencySet resolveVendor(GolangPackageModule module) {
        Path vendorPath = vendorDir(module).toPath();
        VendorDirectoryVistor vistor = new VendorDirectoryVistor(module, vendorPath, resolvers);
        try {
            Files.walkFileTree(vendorPath, Collections.<FileVisitOption>emptySet(), VendorDirectoryVistor.MAX_DEPTH, vistor);
        } catch (IOException e) {
            throw new DependencyResolutionException(e);
        }
        return vistor.getDependencies();
    }

    private File vendorDir(GolangPackageModule module) {
        return FileUtils.locate(module.getRootDir(), "vendor");
    }

    @Override
    public boolean accept(GolangPackageModule module) {
        File vendorDir = vendorDir(module);
        return vendorDir.isDirectory();
    }
}
