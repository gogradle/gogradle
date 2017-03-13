package com.github.blindpirate.gogradle.task.go;

import com.github.blindpirate.gogradle.build.TestPatternFilter;
import com.github.blindpirate.gogradle.util.IOUtils;
import com.github.blindpirate.gogradle.util.StringUtils;
import org.gradle.api.Incubating;
import org.gradle.api.Project;
import org.gradle.api.internal.tasks.options.Option;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import javax.inject.Inject;
import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.github.blindpirate.gogradle.common.GoSourceCodeFilter.TEST_GO_FILTER;
import static com.github.blindpirate.gogradle.task.GolangTaskContainer.INSTALL_BUILD_DEPENDENCIES_TASK_NAME;
import static com.github.blindpirate.gogradle.task.GolangTaskContainer.INSTALL_TEST_DEPENDENCIES_TASK_NAME;
import static com.github.blindpirate.gogradle.util.CollectionUtils.asStringList;
import static com.github.blindpirate.gogradle.util.CollectionUtils.isEmpty;
import static com.github.blindpirate.gogradle.util.IOUtils.filterFilesRecursively;
import static com.github.blindpirate.gogradle.util.IOUtils.safeList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class GoTestTask extends Go {
    private static final Logger LOGGER = Logging.getLogger(GoTestTask.class);

    @Inject
    private Project project;

    private List<String> testNamePattern;

    public GoTestTask() {
        dependsOn(INSTALL_BUILD_DEPENDENCIES_TASK_NAME,
                INSTALL_TEST_DEPENDENCIES_TASK_NAME);
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

    private void addTestActions() {
        Collection<File> filesMatchingPatterns = filterMatchedTests();

        if (filesMatchingPatterns.isEmpty()) {
            LOGGER.quiet("No tests matching " + testNamePattern.stream().collect(joining("/")) + ", skip.");
        } else {
            LOGGER.quiet("Found " + filesMatchingPatterns.size() + " tests to run.");

            Map<File, List<File>> parentDirToFiles = groupByParentDir(filesMatchingPatterns);

            parentDirToFiles.forEach((parentDir, tests) -> {
                List<String> fullPaths = tests.stream()
                        .map(File::getAbsolutePath).collect(toList());
                fullPaths.addAll(getAllNonTestGoFiles(parentDir));
                doLast(task -> buildManager.go(asStringList("test", fullPaths), null));
            });
        }
    }

    private Collection<File> filterMatchedTests() {
        TestPatternFilter filter = TestPatternFilter.withPattern(testNamePattern);
        return filterFilesRecursively(project.getRootDir(), filter);
    }

    private void addTestAllAction() {
        // https://golang.org/cmd/go/#hdr-Description_of_package_lists
//        doLast(task -> buildManager.go(Arrays.asList("test", "./...", "-coverprofile=.gogradle/coverage.out"), null));

        Collection<File> allTestFiles = IOUtils.filterFilesRecursively(project.getRootDir(), TEST_GO_FILTER);
        Map<File, List<File>> groupByParentDir = groupByParentDir(allTestFiles);
    }

    private Map<File, List<File>> groupByParentDir(Collection<File> files) {
        return files.stream()
                .collect(Collectors.groupingBy(File::getParentFile));
    }

    private List<String> getAllNonTestGoFiles(File dir) {
        List<String> names = safeList(dir);
        return names.stream()
                .filter(name -> name.endsWith(".go"))
                .filter(name -> !StringUtils.startsWithAny(name, "_", "."))
                .filter(name -> !name.endsWith("_test.go"))
                .map(name -> new File(dir, name))
                .map(File::getAbsolutePath)
                .collect(toList());
    }
}
