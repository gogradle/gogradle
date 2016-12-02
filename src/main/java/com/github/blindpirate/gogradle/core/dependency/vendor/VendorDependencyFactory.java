package com.github.blindpirate.gogradle.core.dependency.vendor;

import com.github.blindpirate.gogradle.core.GolangPackageModule;
import com.github.blindpirate.gogradle.core.VendorPackageModule;
import com.github.blindpirate.gogradle.core.dependency.resolve.DependencyResolutionException;
import com.github.blindpirate.gogradle.core.pack.GitHubPackageResolver;
import com.github.blindpirate.golang.plugin.core.dependency.DefaultDependencySet;
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet;
import com.github.blindpirate.gogradle.core.dependency.DependencyFactory;
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
 * {@link VendorPackageModule }}
 */
public class VendorDependencyFactory implements DependencyFactory {
    List<PackageNameResolver> resolvers = new ArrayList<>();

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
            Files.walkFileTree(vendorPath, Collections.<FileVisitOption>emptySet(), -1, vistor);
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
