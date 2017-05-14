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

import com.github.blindpirate.gogradle.util.StringUtils;

import java.io.File;
import java.util.function.Predicate;

import static com.github.blindpirate.gogradle.core.dependency.produce.SourceCodeDependencyFactory.TESTDATA_DIRECTORY;
import static com.github.blindpirate.gogradle.core.dependency.produce.VendorDependencyFactory.VENDOR_DIRECTORY;
import static com.github.blindpirate.gogradle.util.StringUtils.fileNameEqualsAny;

public class GoSourceCodeFilter extends AbstractFileFilter {

    private Predicate<File> filePredicate;

    public static final GoSourceCodeFilter BUILD_GO_FILTER = withFilePredicate(GoSourceCodeFilter::isBuildGoFile);
    public static final GoSourceCodeFilter TEST_GO_FILTER = withFilePredicate(GoSourceCodeFilter::isTestGoFile);

    protected static boolean isBuildGoFile(File file) {
        return file.getName().endsWith(".go") && !file.getName().endsWith("_test.go");
    }

    protected static boolean isTestGoFile(File file) {
        return file.getName().endsWith("_test.go");
    }

    public static GoSourceCodeFilter withFilePredicate(Predicate<File> predicate) {
        return new GoSourceCodeFilter(predicate);
    }

    protected GoSourceCodeFilter(Predicate<File> filePredicate) {
        this.filePredicate = filePredicate;
    }

    @Override
    protected boolean acceptFile(File file) {
        if (StringUtils.fileNameStartsWithAny(file, "_", ".")) {
            return false;
        }
        return filePredicate.test(file);
    }

    @Override
    protected boolean acceptDir(File dir) {
        if (StringUtils.fileNameStartsWithAny(dir, "_", ".")) {
            return false;
        }
        if (fileNameEqualsAny(dir, TESTDATA_DIRECTORY, VENDOR_DIRECTORY)) {
            return false;
        }
        return true;
    }
}
