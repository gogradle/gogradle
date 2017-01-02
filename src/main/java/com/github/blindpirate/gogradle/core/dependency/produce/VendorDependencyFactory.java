package com.github.blindpirate.gogradle.core.dependency.produce;

import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet;
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency;
import com.github.blindpirate.gogradle.core.dependency.produce.strategy.VendorOnlyProduceStrategy;
import com.github.blindpirate.gogradle.core.pack.PackagePathResolver;
import com.github.blindpirate.gogradle.util.IOUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;


/**
 * A {@link VendorDependencyFactory is a factory that reads vendor directory and resolves them to
 * {@link com.github.blindpirate.gogradle.core.dependency.VendorDependency }
 */
@Singleton
public class VendorDependencyFactory {
    public static final String VENDOR_DIRECTORY = "vendor";

    public static final VendorOnlyProduceStrategy VENDOR_ONLY_PRODUCE_STRATEGY
            = new VendorOnlyProduceStrategy();

    private final PackagePathResolver packagePathResolver;

    @Inject
    public VendorDependencyFactory(PackagePathResolver packagePathResolver) {
        this.packagePathResolver = packagePathResolver;
    }

    public GolangDependencySet produce(ResolvedDependency dependency, File rootDir) {
        if (vendorDirExist(rootDir)) {
            return resolveVendor(dependency, rootDir);
        } else {
            return GolangDependencySet.empty();
        }
    }

    private GolangDependencySet resolveVendor(ResolvedDependency dependency, File rootDir) {
        Path vendorPath = vendorPath(rootDir);
        VendorDirectoryVisitor visitor = new VendorDirectoryVisitor(dependency, vendorPath, packagePathResolver);
        IOUtils.walkFileTreeSafely(vendorPath, visitor);
        return visitor.getDependencies();
    }

    private boolean vendorDirExist(File rootDir) {
        return Files.exists(rootDir.toPath().resolve(VENDOR_DIRECTORY));
    }

    private Path vendorPath(File rootDir) {
        return rootDir.toPath().resolve(VENDOR_DIRECTORY);
    }

}
