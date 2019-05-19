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

import org.apache.commons.io.filefilter.WildcardFileFilter;

import java.io.File;
import java.nio.file.Files;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.github.blindpirate.gogradle.common.GoSourceCodeFilter.SourceSetType.PROJECT_BUILD_FILES_ONLY;
import static com.github.blindpirate.gogradle.common.GoSourceCodeFilter.SourceSetType.PROJECT_TEST_FILES_ONLY;
import static com.github.blindpirate.gogradle.core.GolangConfiguration.BUILD;
import static com.github.blindpirate.gogradle.core.dependency.produce.SourceCodeDependencyFactory.TESTDATA_DIRECTORY;
import static com.github.blindpirate.gogradle.core.dependency.produce.VendorDependencyFactory.VENDOR_DIRECTORY;
import static com.github.blindpirate.gogradle.util.IOUtils.filterFilesRecursively;
import static com.github.blindpirate.gogradle.util.IOUtils.isValidDirectory;
import static com.github.blindpirate.gogradle.util.StringUtils.fileNameEqualsAny;
import static com.github.blindpirate.gogradle.util.StringUtils.fileNameStartsWithDotOrUnderline;

/**
 * Filters go source code satisfying a specific predicate. By default, files/directories whose name starts
 * with _ or . and testdata directory will be discarded no matter what the predicate is.
 */
public class GoSourceCodeFilter extends AbstractFileFilter {
    public enum SourceSetType {
        /**
         * All non-test go files in project directory, not including any files in vendor directory
         */
        PROJECT_BUILD_FILES_ONLY(true, false, false),
        /**
         * All *_test.go files in project directory, not including any files in vendor directory
         */
        PROJECT_TEST_FILES_ONLY(false, true, false),

        /**
         * PROJECT_BUILD_FILES_ONLY + PROJECT_TEST_FILES_ONLY
         */
        PROJECT_ALL_FILES_ONLY(true, true, false),

        /**
         * All non-test go files in project directory as well as all non-test go files in vendor directory
         */
        PROJECT_AND_VENDOR_BUILD_FILES(true, false, true),

        /**
         * All *.go files in project directory as well as all non-test go files in vendor directory
         */
        PROJECT_TEST_AND_VENDOR_BUILD_FILES(true, true, true);

        private boolean containsProjectBuildFiles;
        private boolean containsProjectTestFiles;
        private boolean containsVendorBuildFiles;

        SourceSetType(boolean containsProjectBuildFiles,
                      boolean containsProjectTestFiles,
                      boolean containsVendorBuildFiles) {
            this.containsProjectBuildFiles = containsProjectBuildFiles;
            this.containsProjectTestFiles = containsProjectTestFiles;
            this.containsVendorBuildFiles = containsVendorBuildFiles;
        }
    }

    public static Collection<File> filterGoFiles(File projectDir, String configuration) {
        return BUILD.equals(configuration)
                ? filterGoFiles(projectDir, PROJECT_BUILD_FILES_ONLY)
                : filterGoFiles(projectDir, PROJECT_TEST_FILES_ONLY);
    }

    public static Collection<File> filterGoFiles(File projectDir, SourceSetType sourceSetType) {
        Collection<File> projectGoFiles = filterProjectGoFiles(projectDir, sourceSetType);
        if (sourceSetType.containsVendorBuildFiles && isValidDirectory(new File(projectDir, VENDOR_DIRECTORY))) {
            Set<File> result = new HashSet<>(projectGoFiles);
            result.addAll(filterVendorGoFiles(projectDir));
            return result;
        } else {
            return projectGoFiles;
        }
    }

    /**
     * Filters tests files with specific patterns. Wildcards are supported.
     *
     * @param projectDir the project root directory.
     * @param patterns the pattern list
     * @return all files in the project directory (recursively) matching the patterns
     */
    public static Collection<File> filterTestsMatchingPattern(File projectDir, List<String> patterns) {
        WildcardFileFilter wildcardFileFilter = new WildcardFileFilter(patterns);
        return filterGoFiles(projectDir, PROJECT_TEST_FILES_ONLY)
                .stream()
                .filter(wildcardFileFilter::accept)
                .collect(Collectors.toList());
    }

    private static Collection<File> filterVendorGoFiles(File projectDir) {
        Predicate<File> goFilesPredicate = file -> containThisFile(file, true, false);
        return filterFilesRecursively(new File(projectDir, VENDOR_DIRECTORY), new GoSourceCodeFilter(goFilesPredicate));
    }

    private static Collection<File> filterProjectGoFiles(File projectDir, SourceSetType sourceSetType) {
        Predicate<File> notVendorPredicate = dir -> !isVendorDirectoryOfProject(dir, projectDir);
        Predicate<File> projectGoFilesPredicate = file -> containThisFile(file, sourceSetType);

        return filterFilesRecursively(projectDir, new GoSourceCodeFilter(projectGoFilesPredicate, notVendorPredicate));
    }

    private static boolean containThisFile(File file, SourceSetType sourceSetType) {
        return containThisFile(file, sourceSetType.containsProjectBuildFiles, sourceSetType.containsProjectTestFiles);
    }

    private static boolean containThisFile(File file, boolean containsBuildFiles, boolean containsTestFiles) {
        String fileName = file.getName();
        if (!fileName.endsWith(".go")) {
            return false;
        }
        if (fileName.endsWith("_test.go")) {
            return containsTestFiles;
        }
        return containsBuildFiles;
    }

    private static boolean isVendorDirectoryOfProject(File dir, File projectDir) {
        return VENDOR_DIRECTORY.equals(dir.getName()) && projectDir.equals(dir.getParentFile());
    }

    private Predicate<File> filePredicate;
    private Predicate<File> dirPredicate;

    private GoSourceCodeFilter(Predicate<File> filePredicate) {
        this(filePredicate, dir -> true);
    }

    private GoSourceCodeFilter(Predicate<File> filePredicate, Predicate<File> dirPredicate) {
        this.filePredicate = filePredicate;
        this.dirPredicate = dirPredicate;
    }

    @Override
    protected boolean acceptFile(File file) {
        if (fileNameStartsWithDotOrUnderline(file)) {
            return false;
        }
        return filePredicate.test(file);
    }

    @Override
    protected boolean acceptDir(File dir) {
        if (Files.isSymbolicLink(dir.toPath())) {
            return false;
        }
        if (fileNameStartsWithDotOrUnderline(dir)) {
            return false;
        }
        if (fileNameEqualsAny(dir, TESTDATA_DIRECTORY)) {
            return false;
        }
        return dirPredicate.test(dir);
    }
}
