package com.github.blindpirate.gogradle.core.dependency.resolve;

import com.github.blindpirate.gogradle.core.FileSystemModule;
import com.github.blindpirate.gogradle.core.GolangPackageModule;
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet;
import com.github.blindpirate.gogradle.core.exceptions.DependencyResolutionException;
import com.github.blindpirate.gogradle.core.pack.PackageNameResolver;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

import static com.github.blindpirate.gogradle.GolangPluginSetting.MAX_DIRECTORY_WALK_DEPTH;


/**
 * A {@link VendorDependencyFactory is a factory that reads vendor directory and resolves them to
 * {@link FileSystemModule }
 */
@Singleton
public class VendorDependencyFactory implements DependencyFactory {
    static final String VENDOR_DIRECTORY = "vendor";

    private final PackageNameResolver packageNameResolver;

    @Inject
    public VendorDependencyFactory(PackageNameResolver packageNameResolver) {
        this.packageNameResolver = packageNameResolver;
    }

    @Override
    public Optional<GolangDependencySet> produce(GolangPackageModule module) {
        if (vendorDirExist(module)) {
            return Optional.of(resolveVendor(module));
        } else {
            return Optional.empty();
        }
    }

    private GolangDependencySet resolveVendor(GolangPackageModule module) {
        Path vendorPath = vendorPath(module);
        FileSystemModule fileSystemModule = (FileSystemModule) module;
        VendorDirectoryVisitor visitor = new VendorDirectoryVisitor(fileSystemModule, packageNameResolver);
        try {
            Files.walkFileTree(vendorPath, Collections.<FileVisitOption>emptySet(), MAX_DIRECTORY_WALK_DEPTH, visitor);
        } catch (IOException e) {
            throw DependencyResolutionException.cannotResolveVendor(module, e);
        }
        return visitor.getDependencies();
    }

    private boolean vendorDirExist(GolangPackageModule module) {
        return Files.exists(module.getRootDir().resolve(VENDOR_DIRECTORY));
    }

    private Path vendorPath(GolangPackageModule module) {
        return module.getRootDir().resolve(VENDOR_DIRECTORY);
    }

}
