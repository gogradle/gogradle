package com.github.blindpirate.golang.plugin.core.task;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

/**
 * This task perform preparation such as Go executable and GOPATH.
 */
class PrepareTask extends DefaultTask {

    @TaskAction
    public void task() {
        getProject().setProperty("", determineGoBinary());
    }

    /**
     * determine version and return its local file path
     *
     * @return
     */
    private String determineGoBinary() {
        return null;
    }
}
