package com.github.blindpirate.gogradle.task;

import com.github.blindpirate.gogradle.GolangPluginSetting;
import com.github.blindpirate.gogradle.util.CollectionUtils;
import org.gradle.api.Task;

import javax.inject.Inject;
import java.util.List;

import static java.util.Arrays.asList;

public class GoCoverTask extends Go {
    @Inject
    private GolangPluginSetting setting;

    private static final List<String> GO_COVER_ARGS = asList(
            "tool",
            "cover",
            "-html=.gogradle/coverage.out",
            "-o",
            ".gogradle/coverage.html");

    public GoCoverTask() {
        dependsOn(GolangTaskContainer.TEST_TASK_NAME);
    }

    protected void doAddDefaultAction() {
        doLast(this::execute);
    }

    private void execute(Task task) {
        buildManager.go(CollectionUtils.asStringList(GO_COVER_ARGS, setting.getPackagePath()), null);
    }
}
