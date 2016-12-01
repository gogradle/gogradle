package com.github.blindpirate.golang.plugin.core.task;

import com.github.blindpirate.golang.plugin.core.cache.CacheDirectoryManager;
import com.github.blindpirate.golang.plugin.crossplatform.GoBinaryManager;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;

/**
 * This task perform preparation such as Go executable and GOPATH.
 */
class PrepareTask extends DefaultTask {

    @Inject
    private GoBinaryManager binaryManager;

    @Inject
    private CacheDirectoryManager cacheDirectoryManager;

    @Inject
    private GopathManager gopathManager;

    @TaskAction
    public void task() {
        //make sure the go binary is properly installed
        binaryManager.binaryPath();
    }

}
