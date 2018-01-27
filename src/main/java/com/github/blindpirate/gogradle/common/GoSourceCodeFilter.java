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
import com.google.common.collect.ImmutableMap;

import java.io.File;
import java.util.Map;
import java.util.function.Predicate;

import static com.github.blindpirate.gogradle.core.GolangConfiguration.BUILD;
import static com.github.blindpirate.gogradle.core.GolangConfiguration.TEST;
import static com.github.blindpirate.gogradle.core.dependency.produce.SourceCodeDependencyFactory.TESTDATA_DIRECTORY;
import static com.github.blindpirate.gogradle.core.dependency.produce.VendorDependencyFactory.VENDOR_DIRECTORY;
import static com.github.blindpirate.gogradle.util.StringUtils.fileNameEqualsAny;

/**
 * Filters go source code satisfying a specific predicate. By default, files/directories whose name starts
 * with _ or . and vendor/testdata directory will be discarded no matter what the predicate is.
 * <p>
 * There are two pre-defined filters to filter go build source code and go test source code.
 */
public class GoSourceCodeFilter extends AbstractFileFilter {
    private static final Predicate<File> BUILD_GO_FILE_PREDICATE = GoSourceCodeFilter::isBuildGoFile;
    private static final Predicate<File> TEST_GO_FILE_PREDICATE = GoSourceCodeFilter::isTestGoFile;
    private static final Predicate<File> ALL_GO_FILE_PREDICATE = BUILD_GO_FILE_PREDICATE.or(TEST_GO_FILE_PREDICATE);

    public static final GoSourceCodeFilter BUILD_GO_FILTER = withPredicate(BUILD_GO_FILE_PREDICATE);
    public static final GoSourceCodeFilter TEST_GO_FILTER = withPredicate(TEST_GO_FILE_PREDICATE);
    public static final GoSourceCodeFilter ALL_GO_FILTER = withPredicate(ALL_GO_FILE_PREDICATE);

    public static final Map<String, Predicate<File>> PREDICATES = ImmutableMap.of(
            BUILD, GoSourceCodeFilter::isBuildGoFile,
            TEST, GoSourceCodeFilter::isTestGoFile);


    public static final Map<String, GoSourceCodeFilter> FILTERS = ImmutableMap.of(
            BUILD, BUILD_GO_FILTER,
            TEST, TEST_GO_FILTER);

    private Predicate<File> filePredicate;

    protected static boolean isBuildGoFile(File file) {
        return file.getName().endsWith(".go") && !file.getName().endsWith("_test.go");
    }

    protected static boolean isTestGoFile(File file) {
        return file.getName().endsWith("_test.go");
    }

    public static GoSourceCodeFilter withPredicate(Predicate<File> predicate) {
        return new GoSourceCodeFilter(predicate);
    }

    protected GoSourceCodeFilter(Predicate<File> filePredicate) {
        this.filePredicate = filePredicate;
    }

    @Override
    protected boolean acceptFile(File file) {
        if (StringUtils.fileNameStartsWithDotOrUnderline(file)) {
            return false;
        }
        return filePredicate.test(file);
    }

    @Override
    protected boolean acceptDir(File dir) {
        if (StringUtils.fileNameStartsWithDotOrUnderline(dir)) {
            return false;
        }
        if (fileNameEqualsAny(dir, TESTDATA_DIRECTORY, VENDOR_DIRECTORY)) {
            return false;
        }
        return true;
    }
}
