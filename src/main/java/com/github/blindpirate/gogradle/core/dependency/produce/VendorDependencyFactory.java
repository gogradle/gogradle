package com.github.blindpirate.gogradle.core.dependency.produce;

import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet;
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency;
import com.github.blindpirate.gogradle.core.dependency.VendorResolvedDependency;
import com.github.blindpirate.gogradle.core.pack.PackagePathResolver;
import com.github.blindpirate.gogradle.util.IOUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;


/**
 * A {@link VendorDependencyFactory is a factory that reads vendor directory and resolves them to
 * {@link VendorResolvedDependency }
 */
@Singleton
public class VendorDependencyFactory {
    public static final String VENDOR_DIRECTORY = "vendor";

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
        FirstPassVendorDirectoryVisitor firstPassVistor =
                new FirstPassVendorDirectoryVisitor(vendorPath, packagePathResolver);
        IOUtils.walkFileTreeSafely(vendorPath, firstPassVistor);
        SecondPassVendorDirectoryVisitor secondPassVisitor =
                new SecondPassVendorDirectoryVisitor(dependency, vendorPath, packagePathResolver);
        IOUtils.walkFileTreeSafely(vendorPath, secondPassVisitor);
        return secondPassVisitor.getDependencies();
    }

    private boolean vendorDirExist(File rootDir) {
        return new File(rootDir, VENDOR_DIRECTORY).exists();
    }

    private Path vendorPath(File rootDir) {
        return rootDir.toPath().resolve(VENDOR_DIRECTORY);
    }

}
