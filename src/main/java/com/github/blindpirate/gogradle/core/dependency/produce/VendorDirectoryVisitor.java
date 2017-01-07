package com.github.blindpirate.gogradle.core.dependency.produce;

import com.github.blindpirate.gogradle.core.GolangPackage;
import com.github.blindpirate.gogradle.core.dependency.GolangDependency;
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet;
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency;
import com.github.blindpirate.gogradle.core.dependency.VendorResolvedDependency;
import com.github.blindpirate.gogradle.core.pack.PackagePathResolver;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Analyze vendor directory to generate dependencies.
 */
public class VendorDirectoryVisitor extends SimpleFileVisitor<Path> {
    private static final Logger LOGGER = Logging.getLogger(VendorDirectoryVisitor.class);

    private ResolvedDependency parent;

    private PackagePathResolver resolver;

    private Path parentVendor;

    private GolangDependencySet dependencies = new GolangDependencySet();

    public GolangDependencySet getDependencies() {
        return dependencies;
    }

    public VendorDirectoryVisitor(
            ResolvedDependency parent,
            Path parentVendor,
            PackagePathResolver resolver) {
        this.parent = parent;
        this.resolver = resolver;
        this.parentVendor = parentVendor;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path currentPath, BasicFileAttributes attrs)
            throws IOException {
        if (currentPath == parentVendor) {
            return FileVisitResult.CONTINUE;
        }

        // relative path, i.e "github.com/a/b"
        String packagePath = parentVendor.relativize(currentPath).toString();

        GolangPackage golangPackage = resolver.produce(packagePath).get();
        if (golangPackage != GolangPackage.INCOMPLETE) {
            // current path is root of a repo
            LOGGER.debug("Produce package {}.", packagePath);
            dependencies.add(createDependency(packagePath, currentPath));
            return FileVisitResult.SKIP_SUBTREE;
        } else {
            LOGGER.debug("Cannot produce package with path {}, skip.", packagePath);
            return FileVisitResult.CONTINUE;
        }
    }


    private GolangDependency createDependency(String packagePath, Path rootPath) {
        return VendorResolvedDependency.fromParent(packagePath, parent, rootPath.toFile());
    }
}
