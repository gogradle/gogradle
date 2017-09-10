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

import com.github.blindpirate.gogradle.GolangPluginSetting;
import com.github.blindpirate.gogradle.build.BuildManager;
import com.github.blindpirate.gogradle.build.TestPatternFilter;
import com.github.blindpirate.gogradle.common.LineCollector;
import com.github.blindpirate.gogradle.task.AbstractGolangTask;
import com.github.blindpirate.gogradle.unsafe.GradleInternalAPI;
import com.github.blindpirate.gogradle.util.IOUtils;
import com.github.blindpirate.gogradle.util.StringUtils;
import com.google.common.collect.Lists;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.gradle.api.Incubating;
import org.gradle.api.internal.tasks.options.Option;
import org.gradle.api.internal.tasks.testing.junit.result.TestClassResult;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.github.blindpirate.gogradle.common.GoSourceCodeFilter.TEST_GO_FILTER;
import static com.github.blindpirate.gogradle.task.GolangTaskContainer.VENDOR_TASK_NAME;
import static com.github.blindpirate.gogradle.task.go.GoCover.COVERAGE_PROFILES_PATH;
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

public class GoTest extends AbstractGolangTask {
    private static final Logger LOGGER = Logging.getLogger(GoTest.class);

    private static final String REWRITE_SCRIPT_RESOURCE = "test/rewrite.html";

    @Inject
    private GolangPluginSetting setting;

    @Inject
    private GoTestStdoutExtractor extractor;

    @Inject
    private BuildManager buildManager;

    private List<String> testNamePattern;

    private boolean generateCoverageProfile = true;

    private boolean coverageProfileGenerated = false;

    private boolean continueWhenFail;

    public GoTest() {
        setDescription("Run all tests.");
        dependsOn(VENDOR_TASK_NAME);
    }

    public void setContinueWhenFail(boolean continueWhenFail) {
        this.continueWhenFail = continueWhenFail;
    }

    public boolean isCoverageProfileGenerated() {
        return coverageProfileGenerated;
    }

    public void setGenerateCoverageProfile(boolean generateCoverageProfile) {
        this.generateCoverageProfile = generateCoverageProfile;
    }

    @Option(option = "tests", description = "Sets test class or method name to be included, '*' is supported.")
    @Incubating
    public GoTest setTestNamePattern(List<String> testNamePattern) {
        this.testNamePattern = testNamePattern;
        return this;
    }

    @TaskAction
    public void run() {
        prepareCoverageProfileDir();

        List<TestClassResult> testResults = doTest();

        File reportDir = generateTestReport(testResults);

        rewritePackageName(reportDir);
        reportErrorIfNecessary(testResults, reportDir);
    }

    private Collection<File> filterMatchedTests() {
        TestPatternFilter filter = TestPatternFilter.withPattern(testNamePattern);
        return filterFilesRecursively(getProject().getProjectDir(), filter);
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
        Path relativeToProjectRoot = getProject().getProjectDir().toPath().relativize(dir.toPath());
        Path importPath = Paths.get(setting.getPackagePath()).resolve(relativeToProjectRoot);
        return toUnixString(importPath);
    }


    private File generateTestReport(List<TestClassResult> testResults) {
        File reportDir = new File(getProject().getProjectDir(), ".gogradle/reports/test");
        GradleInternalAPI.renderTestReport(testResults, reportDir);
        return reportDir;
    }

    private List<TestClassResult> doTest() {
        List<TestClassResult> ret = new ArrayList<>();

        Map<File, List<File>> parentDirToTestFiles = determineTestPattern();

        parentDirToTestFiles.forEach((parentDir, testFiles) -> {
            String packageImportPath = dirToImportPath(parentDir);
            PackageTestResult result = doSingleTest(packageImportPath, parentDir, testFiles);

            List<TestClassResult> resultOfSinglePackage = extractor.extractTestResult(result);
            logResult(packageImportPath, resultOfSinglePackage);
            ret.addAll(resultOfSinglePackage);
        });
        return ret;
    }

    private Map<File, List<File>> determineTestPattern() {
        if (isEmpty(testNamePattern)) {
            // https://golang.org/cmd/go/#hdr-Description_of_package_lists
            Collection<File> allTestFiles = filterFilesRecursively(getProject().getProjectDir(), TEST_GO_FILTER);
            return groupByParentDir(allTestFiles);
        } else {
            Collection<File> filesMatchingPatterns = filterMatchedTests();

            if (filesMatchingPatterns.isEmpty()) {
                LOGGER.quiet("No tests matching " + testNamePattern.stream().collect(joining("/")) + ", skip.");
                return Collections.emptyMap();
            } else {
                LOGGER.quiet("Found " + filesMatchingPatterns.size() + " files to test.");

                Map<File, List<File>> parentDirToFiles = groupByParentDir(filesMatchingPatterns);

                parentDirToFiles.forEach((parentDir, tests) -> tests.addAll(getAllNonTestGoFiles(parentDir)));

                return parentDirToFiles;
            }
        }
    }


    private void reportErrorIfNecessary(List<TestClassResult> results, File reportDir) {
        int totalFailureCount = results.stream().mapToInt(TestClassResult::getFailuresCount).sum();
        String message = "There are " + totalFailureCount + " failed tests. Please see "
                + StringUtils.toUnixString(new File(reportDir, "index.html"))
                + " for more details.";
        if (continueWhenFail) {
            LOGGER.error(message);
        } else if (totalFailureCount > 0) {
            throw new IllegalStateException(message);
        }
    }

    private void prepareCoverageProfileDir() {
        File coverageDir = new File(getProject().getProjectDir(), COVERAGE_PROFILES_PATH);
        forceMkdir(coverageDir);
        clearDirectory(coverageDir);
    }

    private PackageTestResult doSingleTest(String importPath, File parentDir, List<File> testFiles) {
        LineCollector lineCollector = new LineCollector();
        List<String> args = isCommandLineArguments()
                ? asStringList("test", "-v", IOUtils.collectFileNames(testFiles))
                : Lists.newArrayList("test", "-v", importPath);


        if (generateCoverageProfile) {
            File profilesPath = new File(getProject().getProjectDir(), COVERAGE_PROFILES_PATH + "/"
                    + encodeInternally(importPath));
            args.add("-coverprofile=" + StringUtils.toUnixString(profilesPath.getAbsolutePath()));
            coverageProfileGenerated = true;
        }

        int retcode = buildManager.go(args, emptyMap(), lineCollector, lineCollector, true);

        if (LOGGER.isInfoEnabled()) {
            if (isCommandLineArguments()) {
                LOGGER.info("Result:");
            } else {
                LOGGER.info("Result of package {}:", importPath);
            }

            LOGGER.info(lineCollector.getOutput());
        }

        return PackageTestResult.builder()
                .withPackagePath(importPath)
                .withStdout(lineCollector.getLines())
                .withTestFiles(testFiles)
                .withCode(retcode)
                .build();
    }

    private boolean isCommandLineArguments() {
        return !isEmpty(testNamePattern);
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

    private void rewritePackageName(File reportDir) {
        Collection<File> htmlFiles = filterFilesRecursively(
                reportDir,
                new SuffixFileFilter(".html"),
                TrueFileFilter.INSTANCE);
        String rewriteScript = IOUtils.toString(
                GoTest.class.getClassLoader().getResourceAsStream(REWRITE_SCRIPT_RESOURCE));
        htmlFiles.forEach(htmlFile -> {
            String content = IOUtils.toString(htmlFile);
            content = content.replace("</body>", "</body>" + rewriteScript);
            IOUtils.write(htmlFile, content);
        });
    }

}
