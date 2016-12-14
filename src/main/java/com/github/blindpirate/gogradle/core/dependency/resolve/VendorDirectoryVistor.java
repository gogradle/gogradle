package com.github.blindpirate.gogradle.core.dependency.resolve;

import com.github.blindpirate.gogradle.core.GolangPackageModule;
import com.github.blindpirate.gogradle.core.pack.VendorPackageNameResolveResult;
import com.github.blindpirate.gogradle.core.pack.VendorResolveContext;
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet;
import com.github.blindpirate.gogradle.core.pack.PackageNameResolver;
import com.github.blindpirate.gogradle.util.FactoryUtil;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

/**
 * Analyze vendor directory to generate dependencies.
 */
public class VendorDirectoryVistor extends SimpleFileVisitor<Path> {

    public static final int MAX_DEPTH = 100;

    private Path vendorPath;

    private List<PackageNameResolver> resolvers;

    private VendorResolveContext context;

    private GolangDependencySet dependencies = new GolangDependencySet();

    public GolangDependencySet getDependencies() {
        return dependencies;
    }

    public VendorDirectoryVistor(GolangPackageModule module,
                                 Path vendorPath,
                                 List<PackageNameResolver> resolvers) {
        this.vendorPath = vendorPath;
        this.context = new VendorResolveContext(module);
        this.resolvers = resolvers;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
            throws IOException {
        if (dir.equals(vendorPath)) {
            // ignore vendor root
            return FileVisitResult.CONTINUE;
        }

        context.setCurrentPath(vendorPath.relativize(dir));

        VendorPackageNameResolveResult result = FactoryUtil.produce(resolvers, context).get();

        if (result.isFinished()) {
            dependencies.add(result.getDependency());
            return FileVisitResult.SKIP_SUBTREE;
        } else {
            return FileVisitResult.CONTINUE;
        }
    }
}
