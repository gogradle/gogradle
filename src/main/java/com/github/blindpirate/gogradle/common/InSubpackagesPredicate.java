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

package com.github.blindpirate.gogradle.common;

import com.github.blindpirate.gogradle.core.dependency.GolangDependency;

import java.io.File;
import java.nio.file.Path;
import java.util.Set;
import java.util.function.Predicate;

public class InSubpackagesPredicate implements Predicate<File> {
    private boolean alwaysTrue;
    private File rootDir;
    private Set<String> subpackages;

    public static InSubpackagesPredicate withRootDirAndSubpackages(File rootDir, Set<String> subpackages) {
        InSubpackagesPredicate ret = new InSubpackagesPredicate();
        if (subpackages.contains(GolangDependency.ALL_DESCENDANTS)) {
            ret.alwaysTrue = true;
        } else {
            ret.rootDir = rootDir;
            ret.subpackages = subpackages;
        }
        return ret;
    }

    private InSubpackagesPredicate() {
    }

    @Override
    public boolean test(File file) {
        if (alwaysTrue) {
            return true;
        }
        return subpackages.stream().anyMatch(subpackage -> fileIsInSubpackage(file, rootDir, subpackage));
    }

    private boolean fileIsInSubpackage(File file, File rootDir, String subpackage) {
        if (GolangDependency.ONLY_CURRENT_FILES.equals(subpackage)) {
            return file.isFile() && file.getParentFile().equals(rootDir);
        } else {
            Path subpackagePath = rootDir.toPath().resolve(subpackage);
            return file.toPath().startsWith(subpackagePath.normalize());
        }
    }
}
