package com.github.blindpirate.gogradle.core.dependency.produce;

import com.github.blindpirate.gogradle.core.GolangPackage;
import com.github.blindpirate.gogradle.core.VcsGolangPackage;
import com.github.blindpirate.gogradle.core.pack.PackagePathResolver;

import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * First pass does not try to produce dependencies, it aims at analyzing package paths since
 * there may be some unrecognized paths in vendor directory.
 */
public class FirstPassVendorDirectoryVisitor extends SimpleFileVisitor<Path> {

    private final PackagePathResolver packagePathResolver;

    private final Path parentVendor;

    public FirstPassVendorDirectoryVisitor(Path parentVendor,
                                           PackagePathResolver packagePathResolver) {
        this.packagePathResolver = packagePathResolver;
        this.parentVendor = parentVendor;
    }

    public FileVisitResult preVisitDirectory(Path currentPath, BasicFileAttributes attrs) {
        if (currentPath == parentVendor) {
            return FileVisitResult.CONTINUE;
        }

        // relative path, i.e "github.com/a/b"
        String packagePath = parentVendor.relativize(currentPath).toString();
        GolangPackage golangPackage = packagePathResolver.produce(packagePath).get();
        if (golangPackage instanceof VcsGolangPackage) {
            return FileVisitResult.SKIP_SUBTREE;
        } else {
            return FileVisitResult.CONTINUE;
        }

    }
}
