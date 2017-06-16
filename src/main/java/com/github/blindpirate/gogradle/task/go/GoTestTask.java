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

package com.github.blindpirate.gogradle.task.go;

import com.github.blindpirate.gogradle.Go;
import com.github.blindpirate.gogradle.GolangPluginSetting;
import com.github.blindpirate.gogradle.build.TestPatternFilter;
import com.github.blindpirate.gogradle.common.LineCollector;
import com.github.blindpirate.gogradle.unsafe.GradleInternalAPI;
import com.github.blindpirate.gogradle.util.IOUtils;
import com.github.blindpirate.gogradle.util.StringUtils;
import com.google.common.collect.Lists;
import groovy.lang.Closure;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.gradle.api.Action;
import org.gradle.api.Incubating;
import org.gradle.api.Task;
import org.gradle.api.internal.tasks.options.Option;
import org.gradle.api.internal.tasks.testing.junit.result.TestClassResult;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import javax.inject.Inject;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static com.github.blindpirate.gogradle.common.GoSourceCodeFilter.TEST_GO_FILTER;
import static com.github.blindpirate.gogradle.task.GolangTaskContainer.VENDOR_TASK_NAME;
import static com.github.blindpirate.gogradle.task.go.GoCoverTask.COVERAGE_PROFILES_PATH;
import static com.github.blindpirate.gogradle.util.CollectionUtils.asStringList;
import static com.github.blindpirate.gogradle.util.CollectionUtils.isEmpty;
import static com.github.blindpirate.gogradle.util.IOUtils.clearDirectory;
import static com.github.blindpirate.gogradle.util.IOUtils.encodeInternally;
import static com.github.blindpirate.gogradle.util.IOUtils.filterFilesRecursively;
import static com.github.blindpirate.gogradle.util.IOUtils.forceMkdir;
import static com.github.blindpirate.gogradle.util.IOUtils.safeListFiles;
import static com.github.blindpirate.gogradle.util.StringUtils.fileNameEndsWithAny;
import static com.github.blindpirate.gogradle.util.StringUtils.fileNameStartsWithAny;
import static com.github.blindpirate.gogradle.util.StringUtils.toUnixString;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class GoTestTask extends Go {
    private static final Logger LOGGER = Logging.getLogger(GoTestTask.class);

    private static final String REWRITE_SCRIPT_RESOURCE = "test/rewrite.html";

    @Inject
    private GolangPluginSetting setting;

    @Inject
    private GoTestStdoutExtractor extractor;

    private List<String> testNamePattern;

    private boolean generateCoverageProfile = true;

    private boolean coverageProfileGenerated = false;

    public GoTestTask() {
        dependsOn(VENDOR_TASK_NAME);
    }

    public boolean isCoverageProfileGenerated() {
        return coverageProfileGenerated;
    }

    public void setGenerateCoverageProfile(boolean generateCoverageProfile) {
        this.generateCoverageProfile = generateCoverageProfile;
    }

    @Option(option = "tests", description = "Sets test class or method name to be included, '*' is supported.")
    @Incubating
    public GoTestTask setTestNamePattern(List<String> testNamePattern) {
        this.testNamePattern = testNamePattern;
        return this;
    }

    @Override
    protected void doAddDefaultAction() {
        super.doLast(new TestPackagesAction());
    }

    @Override
    public Task doLast(final Closure closure) {
        warnNoTestReport();
        return super.doLast(closure);
    }

    @Override
    public Task doFirst(final Closure closure) {
        warnNoTestReport();
        return super.doFirst(closure);
    }

    private void warnNoTestReport() {
        LOGGER.warn("WARNING: test report is not supported in customized test action.");
    }

    public Task leftShift(final Closure action) {
        throw new UnsupportedOperationException("Left shift is not supported since it's deprecated officially");
    }

    private Collection<File> filterMatchedTests() {
        TestPatternFilter filter = TestPatternFilter.withPattern(testNamePattern);
        return filterFilesRecursively(getProject().getRootDir(), filter);
    }

    private Map<File, List<File>> groupByParentDir(Collection<File> files) {
        return files.stream()
                .collect(groupingBy(File::getParentFile));
    }

    private List<File> getAllNonTestGoFiles(File dir) {
        return safeListFiles(dir).stream()
                .filter(file -> file.getName().endsWith(".go"))
                .filter(file -> !fileNameStartsWithAny(file, "_", "."))
                .filter(file -> !fileNameEndsWithAny(file, "_test.go"))
                .filter(File::isFile)
                .collect(toList());
    }

    private String dirToImportPath(File dir) {
        Path relativeToProjectRoot = getProject().getRootDir().toPath().relativize(dir.toPath());
        Path importPath = Paths.get(setting.getPackagePath()).resolve(relativeToProjectRoot);
        return toUnixString(importPath);
    }

    private class TestPackagesAction implements Action<Task> {
        private Map<File, List<File>> parentDirToTestFiles = emptyMap();

        private boolean isCommandLineArguments;

        @Override
        public void execute(Task task) {
            determineTestPattern();
            prepareCoverageProfileDir();

            List<TestClassResult> testResults = doTest();

            File reportDir = generateTestReport(testResults);

            rewritePackageName(reportDir);
            reportErrorIfNecessary(testResults, reportDir);
        }

        private File generateTestReport(List<TestClassResult> testResults) {
            File reportDir = new File(getProject().getRootDir(), ".gogradle/reports/test");
            GradleInternalAPI.renderTestReport(testResults, reportDir);
            return reportDir;
        }

        private List<TestClassResult> doTest() {
            List<TestClassResult> ret = new ArrayList<>();

            parentDirToTestFiles.forEach((parentDir, testFiles) -> {
                String packageImportPath = dirToImportPath(parentDir);
                PackageTestResult result = doSingleTest(packageImportPath, parentDir, testFiles);

                List<TestClassResult> resultOfSinglePackage = extractor.extractTestResult(result);
                logResult(packageImportPath, resultOfSinglePackage);
                ret.addAll(resultOfSinglePackage);
            });
            return ret;
        }

        private void determineTestPattern() {
            if (isEmpty(testNamePattern)) {
                // https://golang.org/cmd/go/#hdr-Description_of_package_lists
                Collection<File> allTestFiles = filterFilesRecursively(getProject().getRootDir(), TEST_GO_FILTER);
                this.parentDirToTestFiles = groupByParentDir(allTestFiles);
                this.isCommandLineArguments = false;
            } else {
                Collection<File> filesMatchingPatterns = filterMatchedTests();

                if (filesMatchingPatterns.isEmpty()) {
                    LOGGER.quiet("No tests matching " + testNamePattern.stream().collect(joining("/")) + ", skip.");
                } else {
                    LOGGER.quiet("Found " + filesMatchingPatterns.size() + " files to test.");

                    Map<File, List<File>> parentDirToFiles = groupByParentDir(filesMatchingPatterns);

                    parentDirToFiles.forEach((parentDir, tests) -> tests.addAll(getAllNonTestGoFiles(parentDir)));

                    this.parentDirToTestFiles = parentDirToFiles;
                    this.isCommandLineArguments = true;
                }
            }
        }


        private void reportErrorIfNecessary(List<TestClassResult> results, File reportDir) {
            int totalFailureCount = results.stream().mapToInt(TestClassResult::getFailuresCount).sum();
            if (totalFailureCount > 0) {
                throw new IllegalStateException("There are " + totalFailureCount + " failed tests. Please see "
                        + StringUtils.toUnixString(new File(reportDir, "index.html"))
                        + " for more details.");
            }
        }

        private void prepareCoverageProfileDir() {
            File coverageDir = new File(getProject().getRootDir(), COVERAGE_PROFILES_PATH);
            forceMkdir(coverageDir);
            clearDirectory(coverageDir);
        }

        private PackageTestResult doSingleTest(String importPath, File parentDir, List<File> testFiles) {
            LineCollector lineCollector = new LineCollector();
            List<String> args = isCommandLineArguments
                    ? asStringList("test", "-v", IOUtils.collectFileNames(testFiles))
                    : Lists.newArrayList("test", "-v", importPath);


            if (generateCoverageProfile) {
                File profilesPath = new File(getProject().getRootDir(), COVERAGE_PROFILES_PATH + "/"
                        + encodeInternally(importPath));
                args.add("-coverprofile=" + StringUtils.toUnixString(profilesPath.getAbsolutePath()));
                coverageProfileGenerated = true;
            }

            AtomicInteger retcode = new AtomicInteger(0);

            buildManager.go(args, emptyMap(), lineCollector, lineCollector, retcode::set);

            return PackageTestResult.builder()
                    .withPackagePath(importPath)
                    .withStdout(lineCollector.getLines())
                    .withTestFiles(testFiles)
                    .withCode(retcode.get())
                    .build();
        }


        private void logResult(String packagePath, List<TestClassResult> resultOfSinglePackage) {
            int successCount = successCount(resultOfSinglePackage);
            int failureCount = failureCount(resultOfSinglePackage);
            LOGGER.quiet("Test for {} finished, {} completed, {} failed",
                    packagePath,
                    successCount + failureCount,
                    failureCount);
        }

        private int successCount(List<TestClassResult> results) {
            return results.stream()
                    .mapToInt(result ->
                            result.getResults().size() - result.getFailuresCount())
                    .sum();
        }

        private int failureCount(List<TestClassResult> results) {
            return results.stream().mapToInt(TestClassResult::getFailuresCount).sum();
        }
    }

    private void rewritePackageName(File reportDir) {
        Collection<File> htmlFiles = filterFilesRecursively(
                reportDir,
                new SuffixFileFilter(".html"),
                TrueFileFilter.INSTANCE);
        String rewriteScript = IOUtils.toString(
                GoTestTask.class.getClassLoader().getResourceAsStream(REWRITE_SCRIPT_RESOURCE));
        htmlFiles.forEach(htmlFile -> {
            String content = IOUtils.toString(htmlFile);
            content = content.replace("</body>", "</body>" + rewriteScript);
            IOUtils.write(htmlFile, content);
        });
    }


}
