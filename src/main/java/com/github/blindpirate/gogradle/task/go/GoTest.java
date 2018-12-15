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
import com.github.blindpirate.gogradle.common.LineCollector;
import com.github.blindpirate.gogradle.crossplatform.GoBinaryManager;
import com.github.blindpirate.gogradle.task.AbstractGolangTask;
import com.github.blindpirate.gogradle.task.go.test.GoTestResultExtractor;
import com.github.blindpirate.gogradle.unsafe.GradleInternalAPI;
import com.github.blindpirate.gogradle.util.IOUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.gradle.api.Incubating;
import org.gradle.api.tasks.options.Option;
import org.gradle.api.internal.tasks.testing.junit.result.TestClassResult;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static com.github.blindpirate.gogradle.common.GoSourceCodeFilter.SourceSetType.PROJECT_TEST_AND_VENDOR_BUILD_FILES;
import static com.github.blindpirate.gogradle.common.GoSourceCodeFilter.SourceSetType.PROJECT_TEST_FILES_ONLY;
import static com.github.blindpirate.gogradle.common.GoSourceCodeFilter.filterGoFiles;
import static com.github.blindpirate.gogradle.common.GoSourceCodeFilter.filterTestsMatchingPattern;
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
import static com.github.blindpirate.gogradle.util.StringUtils.fileNameStartsWithDotOrUnderline;
import static com.github.blindpirate.gogradle.util.StringUtils.toUnixString;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class GoTest extends AbstractGolangTask {
    private static final Logger LOGGER = Logging.getLogger(GoTest.class);
    private static final String REWRITE_SCRIPT_RESOURCE = "test/rewrite.html";
    private static final String TEST_REPORT_DIR = ".gogradle/reports/test";

    @Inject
    private GolangPluginSetting setting;

    @Inject
    private GoTestResultExtractor extractor;

    @Inject
    private BuildManager buildManager;

    @Inject
    private GoBinaryManager goBinaryManager;

    private List<String> testNamePattern;

    private boolean generateCoverageProfile = true;

    private boolean continueOnFailure;

    private Map<String, String> environment = new HashMap<>();

    public GoTest() {
        setDescription("Run all tests.");
        dependsOn(VENDOR_TASK_NAME);
    }

    public void environment(String key, String value) {
        environment.put(key, value);
    }

    public void environment(Map<String, String> map) {
        environment.putAll(map);
    }

    @Deprecated
    public void setContinueWhenFail(boolean continueOnFailure) {
        LOGGER.warn("continueWhenFail is deprecated, please use continueOnFailure instead.");
        this.continueOnFailure = continueOnFailure;
    }

    public void setContinueOnFailure(boolean continueOnFailure) {
        this.continueOnFailure = continueOnFailure;
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
        setGogradleGlobalContext();

        prepareCoverageProfileDir();
        List<TestClassResult> testResults = doTest();
        generateTestReport(testResults);
        rewritePackageName(getReportDir());
        reportErrorIfNecessary(testResults, getReportDir());
    }

    @Input
    @Optional
    List<String> getTestNamePattern() {
        return testNamePattern;
    }

    @Input
    String getGoVersion() {
        return goBinaryManager.getGoVersion();
    }

    @Input
    List<String> getBuildTags() {
        return setting.getBuildTags();
    }

    @InputFiles
    Collection<File> getAllGoFiles() {
        return filterGoFiles(getProjectDir(), PROJECT_TEST_AND_VENDOR_BUILD_FILES);
    }

    @OutputDirectory
    File getReportDir() {
        return new File(getProjectDir(), TEST_REPORT_DIR);
    }

    @OutputDirectory
    File getCoverageDir() {
        return new File(getProjectDir(), GoCover.COVERAGE_PROFILES_PATH);
    }

    private Map<File, List<File>> groupByParentDir(Collection<File> files) {
        return files.stream()
                .collect(groupingBy(File::getParentFile));
    }

    private List<File> getAllNonTestGoFiles(File dir) {
        return safeListFiles(dir).stream()
                .filter(file -> file.getName().endsWith(".go"))
                .filter(file -> !fileNameStartsWithDotOrUnderline(file))
                .filter(file -> !fileNameEndsWithAny(file, "_test.go"))
                .filter(File::isFile)
                .collect(toList());
    }

    private String dirToImportPath(File dir) {
        Path relativeToProjectRoot = getProjectDir().toPath().relativize(dir.toPath());
        Path importPath = Paths.get(setting.getPackagePath()).resolve(relativeToProjectRoot);
        return toUnixString(importPath);
    }


    private void generateTestReport(List<TestClassResult> testResults) {
        GradleInternalAPI.renderTestReport(testResults, getReportDir());
    }

    private List<TestClassResult> doTest() {
        List<TestClassResult> ret = new ArrayList<>();

        Map<File, List<File>> parentDirToTestFiles = determineTestPattern();

        parentDirToTestFiles.forEach((parentDir, testFiles) -> {
            String packageImportPath = dirToImportPath(parentDir);
            PackageTestResult result = doSingleTest(packageImportPath, testFiles);

            List<TestClassResult> resultOfSinglePackage = extractor.extractTestResult(result);
            logResult(packageImportPath, resultOfSinglePackage);
            ret.addAll(resultOfSinglePackage);
        });
        return ret;
    }

    private Map<File, List<File>> determineTestPattern() {
        if (isEmpty(testNamePattern)) {
            // https://golang.org/cmd/go/#hdr-Description_of_package_lists
            Collection<File> allTestFiles = filterGoFiles(getProjectDir(), PROJECT_TEST_FILES_ONLY);
            return groupByParentDir(allTestFiles);
        } else {
            Collection<File> filesMatchingPatterns = filterTestsMatchingPattern(getProjectDir(), testNamePattern);

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
                + toUnixString(new File(reportDir, "index.html"))
                + " for more details.";
        if (continueOnFailure) {
            LOGGER.error(message);
        } else if (totalFailureCount > 0) {
            throw new IllegalStateException(message);
        }
    }

    private void prepareCoverageProfileDir() {
        File coverageDir = new File(getProjectDir(), COVERAGE_PROFILES_PATH);
        forceMkdir(coverageDir);
        clearDirectory(coverageDir);
    }

    private PackageTestResult doSingleTest(String importPath, List<File> testFiles) {
        LineCollector lineCollector = new LineCollector();

        List<String> args = isCommandLineArguments()
                ? asStringList(determineTestParams(), IOUtils.collectFileNames(testFiles))
                : asStringList(determineTestParams(), importPath);


        if (generateCoverageProfile) {
            File profilesPath = new File(getProjectDir(), COVERAGE_PROFILES_PATH + "/"
                    + encodeInternally(importPath) + ".out");
            args.add("-coverprofile=" + toUnixString(profilesPath.getAbsolutePath()));
        }

        Consumer<String> consumer = determineLineConsumer(lineCollector, importPath);

        int retCode = buildManager.go(args, environment, consumer, consumer, true);

        return PackageTestResult.builder()
                .withPackagePath(importPath)
                .withStdout(lineCollector.getLines())
                .withTestFiles(testFiles)
                .withCode(retCode)
                .build();
    }

    private List<String> determineTestParams() {
        if (goBinaryManager.supportTestJsonOutput()) {
            return Arrays.asList("test", "-v", "-json");
        } else {
            return Arrays.asList("test", "-v");
        }
    }

    private Consumer<String> determineLineConsumer(LineCollector lineCollector, String importPath) {
        if (!LOGGER.isInfoEnabled()) {
            return lineCollector;
        } else {
            if (isCommandLineArguments()) {
                LOGGER.info("Result:");
            } else {
                LOGGER.info("Result of package {}:", importPath);
            }
            return lineCollector.andThen(LOGGER::info);
        }
    }

    private boolean isCommandLineArguments() {
        return !isEmpty(testNamePattern);
    }

    private void logResult(String packagePath, List<TestClassResult> resultOfSinglePackage) {
        int failureCount = resultOfSinglePackage.stream().mapToInt(TestClassResult::getFailuresCount).sum();
        int skipCount = resultOfSinglePackage.stream().mapToInt(TestClassResult::getSkippedCount).sum();
        int totalCount = resultOfSinglePackage.stream().mapToInt(TestClassResult::getTestsCount).sum();

        if (skipCount > 0) {
            LOGGER.quiet("Test for {} finished, {} completed, {} failed, {} skipped.",
                    packagePath, totalCount, failureCount, skipCount);
        } else {
            LOGGER.quiet("Test for {} finished, {} completed, {} failed.", packagePath, totalCount, failureCount);
        }
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
