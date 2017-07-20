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

package com.github.blindpirate.gogradle.core.dependency.install;

import com.github.blindpirate.gogradle.common.InSubpackagesPredicate;
import com.github.blindpirate.gogradle.util.IOUtils;

import java.io.File;
import java.io.FileFilter;
import java.util.List;
import java.util.Set;

import static com.github.blindpirate.gogradle.core.dependency.produce.SourceCodeDependencyFactory.TESTDATA_DIRECTORY;
import static com.github.blindpirate.gogradle.core.dependency.produce.VendorDependencyFactory.VENDOR_DIRECTORY;
import static com.github.blindpirate.gogradle.util.StringUtils.endsWithAny;
import static com.github.blindpirate.gogradle.util.StringUtils.fileNameEqualsAny;
import static com.github.blindpirate.gogradle.util.StringUtils.fileNameStartsWithAny;
import static com.github.blindpirate.gogradle.util.StringUtils.startsWithAny;

public class DependencyInstallFileFilter implements FileFilter {
    private InSubpackagesPredicate inSubpackagesPredicate;

    public static DependencyInstallFileFilter subpackagesFilter(File rootDir,
                                                                Set<String> subpackages) {
        InSubpackagesPredicate predicate = InSubpackagesPredicate.withRootDirAndSubpackages(rootDir, subpackages);
        return new DependencyInstallFileFilter(predicate);
    }

    private DependencyInstallFileFilter(InSubpackagesPredicate inSubpackagesPredicate) {
        this.inSubpackagesPredicate = inSubpackagesPredicate;
    }

    @Override
    public boolean accept(File pathname) {
        if (pathname.isDirectory()) {
            return acceptDirectory(pathname);
        } else if (pathname.isFile()) {
            return inSubpackagesPredicate.test(pathname) && acceptFile(pathname);
        } else {
            // symbolic links
            return false;
        }
    }

    private boolean acceptFile(File file) {
        return acceptFileName(file.getName());
    }

    private boolean acceptFileName(String name) {
        if (startsWithAny(name, "_", ".") || endsWithAny(name, "_test.go")) {
            return false;
        }
        return endsWithAny(name, ".go", ".asm", ".s", ".h", ".c", ".a", ".lib")
                || name.contains(".so");
    }

    private boolean acceptDirectory(File dir) {
        if (fileNameEqualsAny(dir, TESTDATA_DIRECTORY, VENDOR_DIRECTORY)) {
            return false;
        }
        if (fileNameStartsWithAny(dir, "_", ".")) {
            return false;
        }

        List<String> files = IOUtils.safeList(dir);
        return files.stream()
                .map(name -> new File(dir, name))
                .anyMatch(this::accept);
    }
}
