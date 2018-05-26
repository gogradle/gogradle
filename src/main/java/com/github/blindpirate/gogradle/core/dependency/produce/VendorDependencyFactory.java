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

import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet;
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency;
import com.github.blindpirate.gogradle.core.dependency.VendorResolvedDependency;
import com.github.blindpirate.gogradle.core.pack.PackagePathResolver;
import com.github.blindpirate.gogradle.util.IOUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.nio.file.Path;

import static com.github.blindpirate.gogradle.core.pack.DefaultPackagePathResolver.AllPackagePathResolvers;


/**
 * A {@link VendorDependencyFactory} is a factory that reads vendor directory and resolves them to
 * {@link VendorResolvedDependency }
 */
@Singleton
public class VendorDependencyFactory {
    public static final String VENDOR_DIRECTORY = "vendor";

    private final PackagePathResolver packagePathResolver;

    @Inject
    public VendorDependencyFactory(@AllPackagePathResolvers PackagePathResolver packagePathResolver) {
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
        // First pass to fill up the cache
        FirstPassVendorDirectoryVisitor firstPassVistor =
                new FirstPassVendorDirectoryVisitor(vendorPath, packagePathResolver);
        IOUtils.walkFileTreeSafely(vendorPath, firstPassVistor);
        // Second pass to do the production
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
