package com.github.blindpirate.gogradle.task.go;

import com.github.blindpirate.gogradle.GolangPluginSetting;
import com.github.blindpirate.gogradle.build.TestPatternFilter;
import com.github.blindpirate.gogradle.util.CollectionUtils;
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
import org.gradle.api.internal.tasks.testing.junit.report.DefaultTestReport;
import org.gradle.api.internal.tasks.testing.junit.result.TestClassResult;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.internal.operations.BuildOperationProcessor;

import javax.inject.Inject;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static com.github.blindpirate.gogradle.common.GoSourceCodeFilter.TEST_GO_FILTER;
import static com.github.blindpirate.gogradle.task.GolangTaskContainer.INSTALL_BUILD_DEPENDENCIES_TASK_NAME;
import static com.github.blindpirate.gogradle.task.GolangTaskContainer.INSTALL_TEST_DEPENDENCIES_TASK_NAME;
import static com.github.blindpirate.gogradle.task.go.GoCoverTask.COVERAGE_PROFILES_PATH;
import static com.github.blindpirate.gogradle.util.CollectionUtils.isEmpty;
import static com.github.blindpirate.gogradle.util.IOUtils.clearDirectory;
import static com.github.blindpirate.gogradle.util.IOUtils.encodeInternally;
import static com.github.blindpirate.gogradle.util.IOUtils.filterFilesRecursively;
import static com.github.blindpirate.gogradle.util.IOUtils.forceMkdir;
import static com.github.blindpirate.gogradle.util.IOUtils.safeListFiles;
import static com.github.blindpirate.gogradle.util.StringUtils.fileNameEndsWithAny;
import static com.github.blindpirate.gogradle.util.StringUtils.fileNameStartsWithAny;
import static com.github.blindpirate.gogradle.util.StringUtils.toUnixString;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class GoTestTask extends Go {
    private static final Logger LOGGER = Logging.getLogger(GoTestTask.class);

    private static final String REWRITE_SCRIPT_RESOURCE = "test/rewrite.html";

    @Inject
    private GolangPluginSetting setting;

    @Inject
    private BuildOperationProcessor buildOperationProcessor;

    @Inject
    private GoTestStdoutExtractor extractor;

    private List<String> testNamePattern;

    private boolean generateCoverageProfile = true;

    private boolean coverageProfileGenerated = false;

    public GoTestTask() {
        dependsOn(INSTALL_BUILD_DEPENDENCIES_TASK_NAME,
                INSTALL_TEST_DEPENDENCIES_TASK_NAME);
    }

    public boolean isGenerateCoverageProfile() {
        return generateCoverageProfile;
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
        if (isEmpty(testNamePattern)) {
            addTestAllAction();
        } else {
            addTestActions();
        }
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

    private void addTestActions() {
        Collection<File> filesMatchingPatterns = filterMatchedTests();

        if (filesMatchingPatterns.isEmpty()) {
            LOGGER.quiet("No tests matching " + testNamePattern.stream().collect(joining("/")) + ", skip.");
        } else {
            LOGGER.quiet("Found " + filesMatchingPatterns.size() + " tests to run.");

            Map<File, List<File>> parentDirToFiles = groupByParentDir(filesMatchingPatterns);

            parentDirToFiles.forEach((parentDir, tests) -> {
                tests.addAll(getAllNonTestGoFiles(parentDir));
            });

            doLast(new TestPackagesAction(parentDirToFiles, true));
        }
    }

    private Collection<File> filterMatchedTests() {
        TestPatternFilter filter = TestPatternFilter.withPattern(testNamePattern);
        return filterFilesRecursively(getProject().getRootDir(), filter);
    }

    private void addTestAllAction() {
        // https://golang.org/cmd/go/#hdr-Description_of_package_lists
        Collection<File> allTestFiles = filterFilesRecursively(getProject().getRootDir(), TEST_GO_FILTER);
        doLast(new TestPackagesAction(groupByParentDir(allTestFiles), false));
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
        private Map<File, List<File>> parentDirToTestFiles;

        private boolean isCommandLineArguments;

        private TestPackagesAction(Map<File, List<File>> parentDirToTestFiles, boolean isCommandLineArguments) {
            this.parentDirToTestFiles = parentDirToTestFiles;
            this.isCommandLineArguments = isCommandLineArguments;
        }

        @Override
        public void execute(Task task) {
            List<TestClassResult> testResults = new ArrayList<>();
            prepareCoverageProfileDir();

            parentDirToTestFiles.forEach((parentDir, testFiles) -> {
                String packageImportPath = dirToImportPath(parentDir);
                List<String> stdout = doSingleTest(parentDir, testFiles);

                PackageTestContext context = PackageTestContext.builder()
                        .withPackagePath(packageImportPath)
                        .withStdout(stdout)
                        .withTestFiles(testFiles)
                        .build();

                List<TestClassResult> resultOfSinglePackage = extractor.extractTestResult(context);
                logResult(packageImportPath, resultOfSinglePackage);
                testResults.addAll(resultOfSinglePackage);
            });

            GoTestResultsProvider provider = new GoTestResultsProvider(testResults);

            File reportDir = new File(getProject().getRootDir(), ".gogradle/reports/test");
            DefaultTestReport report = new DefaultTestReport(buildOperationProcessor);
            report.generateReport(provider, reportDir);

            rewritePackageName(reportDir);

            reportErrorIfNecessary(testResults, reportDir);
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

        private void reportErrorIfNecessary(List<TestClassResult> results, File reportDir) {
            int totalFailureCount = results.stream().mapToInt(TestClassResult::getFailuresCount).sum();
            if (totalFailureCount > 0) {
                throw new IllegalStateException("There are " + totalFailureCount + " failed tests. Please see "
                        + reportDir.getAbsolutePath()
                        + " for more details.");
            }
        }

        private void prepareCoverageProfileDir() {
            File coverageDir = new File(getProject().getRootDir(), COVERAGE_PROFILES_PATH);
            forceMkdir(coverageDir);
            clearDirectory(coverageDir);
        }

        private List<String> doSingleTest(File parentDir, List<File> testFiles) {
            StdoutStderrCollector lineConsumer = new StdoutStderrCollector();
            String importPath = dirToImportPath(parentDir);
            List<String> args;

            if (isCommandLineArguments) {
                args = CollectionUtils.asStringList(
                        "test",
                        "-v",
                        testFiles.stream().map(File::getAbsolutePath).collect(toList()));
            } else {
                args = Lists.newArrayList("test", "-v", importPath);
            }

            if (generateCoverageProfile) {
                File profilesPath = new File(getProject().getRootDir(), COVERAGE_PROFILES_PATH + "/"
                        + encodeInternally(importPath));
                args.add("-coverprofile=" + StringUtils.toUnixString(profilesPath.getAbsolutePath()));
                coverageProfileGenerated = true;
            }

            buildManager.go(args, null, lineConsumer, lineConsumer, code -> {
            });
            return lineConsumer.getStdoutStderr();
        }


        private void logResult(String packagePath, List<TestClassResult> resultOfSinglePackage) {
            LOGGER.quiet("Test for {} finished, {} succeed, {} failed",
                    packagePath,
                    successCount(resultOfSinglePackage),
                    failureCount(resultOfSinglePackage)
            );
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

    private static class StdoutStderrCollector implements Consumer<String> {
        private List<String> lines = new ArrayList<>();

        @Override
        public synchronized void accept(String s) {
            lines.add(s);
        }

        public List<String> getStdoutStderr() {
            return lines;
        }
    }
}
