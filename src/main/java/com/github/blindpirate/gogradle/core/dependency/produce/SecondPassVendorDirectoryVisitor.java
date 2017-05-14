/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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
import static com.github.blindpirate.gogradle.util.StringUtils.toUnixString;

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
        String packagePath = toUnixString(parentVendor.relativize(currentPath));

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
