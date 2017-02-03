package com.github.blindpirate.gogradle.task;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.io.File;

import static com.github.blindpirate.gogradle.GogradleGlobal.GOGRADLE_BUILD_DIR_NAME;
import static com.github.blindpirate.gogradle.util.IOUtils.clearDirectory;
import static com.github.blindpirate.gogradle.util.IOUtils.isValidDirectory;

public class CleanTask extends DefaultTask {

    @Inject
    private Project project;

    @TaskAction
    public void clean() {
        File gogradleBuildDir = new File(project.getRootDir(), GOGRADLE_BUILD_DIR_NAME);
        if (isValidDirectory(gogradleBuildDir)) {
            clearDirectory(gogradleBuildDir);
        }
    }
}
