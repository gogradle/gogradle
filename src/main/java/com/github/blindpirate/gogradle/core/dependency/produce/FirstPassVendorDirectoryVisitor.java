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
import com.github.blindpirate.gogradle.core.VcsGolangPackage;
import com.github.blindpirate.gogradle.core.pack.PackagePathResolver;

import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import static com.github.blindpirate.gogradle.util.StringUtils.toUnixString;

/**
 * First pass does not try to produce dependencies, it aims at analyzing package paths since
 * there may be some unrecognized paths in vendor directory.
 */
public class FirstPassVendorDirectoryVisitor extends SimpleFileVisitor<Path> {

    private final PackagePathResolver packagePathResolver;

    private final Path parentVendor;

    public FirstPassVendorDirectoryVisitor(Path parentVendor, PackagePathResolver packagePathResolver) {
        this.packagePathResolver = packagePathResolver;
        this.parentVendor = parentVendor;
    }

    public FileVisitResult preVisitDirectory(Path currentPath, BasicFileAttributes attrs) {
        if (currentPath == parentVendor) {
            return FileVisitResult.CONTINUE;
        }

        // relative path, i.e "github.com/a/b"
        String packagePath = toUnixString(parentVendor.relativize(currentPath));
        GolangPackage golangPackage = packagePathResolver.produce(packagePath).get();
        if (golangPackage instanceof VcsGolangPackage) {
            return FileVisitResult.SKIP_SUBTREE;
        } else {
            return FileVisitResult.CONTINUE;
        }

    }
}
