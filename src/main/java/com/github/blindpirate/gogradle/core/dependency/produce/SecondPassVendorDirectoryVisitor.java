package com.github.blindpirate.gogradle.core.dependency.produce;

import com.github.blindpirate.gogradle.core.GolangPackage;
import com.github.blindpirate.gogradle.core.UnrecognizedGolangPackage;
import com.github.blindpirate.gogradle.core.VcsGolangPackage;
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

import static com.github.blindpirate.gogradle.core.dependency.produce.VendorDependencyFactory.VENDOR_DIRECTORY;
import static com.github.blindpirate.gogradle.util.IOUtils.isValidDirectory;
import static com.github.blindpirate.gogradle.util.IOUtils.safeList;

/**
 * Analyze vendor directory to generate dependencies.
 */
public class SecondPassVendorDirectoryVisitor extends SimpleFileVisitor<Path> {
    private static final Logger LOGGER = Logging.getLogger(SecondPassVendorDirectoryVisitor.class);

    private ResolvedDependency parent;

    private PackagePathResolver resolver;

    private Path parentVendor;

    private GolangDependencySet dependencies = new GolangDependencySet();

    public GolangDependencySet getDependencies() {
        return dependencies;
    }

    public SecondPassVendorDirectoryVisitor(
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

        if (golangPackage instanceof UnrecognizedGolangPackage) {
            return visitUnrecognizedVendorPackage(packagePath, currentPath);
        } else if (golangPackage instanceof VcsGolangPackage) {
            return visitRepoRoot(packagePath, currentPath);
        } else {
            LOGGER.debug("Cannot produce package with path {}, continue.", packagePath);
            return FileVisitResult.CONTINUE;
        }
    }

    private FileVisitResult visitRepoRoot(String packagePath, Path currentPath) {
        LOGGER.debug("Produce recognized package {}.", packagePath);
        dependencies.add(createDependency(packagePath, currentPath));
        return FileVisitResult.SKIP_SUBTREE;
    }

    private FileVisitResult visitUnrecognizedVendorPackage(String packagePath, Path currentPath) {
        // if currentPath is a empty directory, anyDotGoFileOrVendorDirExist() return false, then nothing happens
        if (anyDotGoFileOrVendorDirExist(currentPath)) {
            LOGGER.debug("Produce unrecognized package {}.", packagePath);
            dependencies.add(createDependency(packagePath, currentPath));
            return FileVisitResult.SKIP_SUBTREE;
        } else {
            LOGGER.debug("Cannot recognize package {}, continue.", packagePath);
            return FileVisitResult.CONTINUE;
        }
    }

    private boolean anyDotGoFileOrVendorDirExist(Path currentPath) {
        return isValidDirectory(currentPath.resolve(VENDOR_DIRECTORY).toFile())
                || safeList(currentPath.toFile()).stream().anyMatch(fileName -> fileName.endsWith(".go"));
    }


    private GolangDependency createDependency(String packagePath, Path rootPath) {
        return VendorResolvedDependency.fromParent(packagePath, parent, rootPath.toFile());
    }
}
