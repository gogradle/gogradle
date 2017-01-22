package com.github.blindpirate.gogradle.task;

import com.github.blindpirate.gogradle.build.BuildManager;
import com.github.blindpirate.gogradle.util.CollectionUtils;
import org.gradle.api.Incubating;
import org.gradle.api.internal.tasks.options.Option;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.util.List;

public class TestTask extends AbstractGolangTask {

    private List<String> testNamePattern;

    @Inject
    private BuildManager buildManager;

    public TestTask() {
        dependsOn(GolangTaskContainer.INSTALL_TEST_DEPENDENCIES_TASK_NAME);
    }

    @Option(option = "tests", description = "Sets test class or method name to be included, '*' is supported.")
    @Incubating
    public TestTask setTestNamePattern(List<String> testNamePattern) {
        this.testNamePattern = testNamePattern;
        return this;
    }

    @TaskAction
    void test() {
        if (CollectionUtils.isEmpty(testNamePattern)) {
            buildManager.test();
        } else {
            buildManager.testWithPatterns(testNamePattern);
        }
    }
}
