package com.github.blindpirate.gogradle.core.task;

import com.github.blindpirate.gogradle.GolangPluginSetting;
import com.github.blindpirate.gogradle.core.cache.GlobalCacheManager;
import com.github.blindpirate.gogradle.crossplatform.GoBinaryManager;
import com.google.inject.Injector;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;

/**
 * This task perform preparation such as Go executable and GOPATH.
 */
public class PrepareTask extends DefaultTask {

    //@Inject
    private GoBinaryManager binaryManager;

    @Inject
    private GlobalCacheManager globalCacheManager;

    @Inject
    private GolangPluginSetting setting;

    @Inject
    private Injector injector;

    @TaskAction
    public void task() {
        //make sure the go binary is properly installed
//        binaryManager.binaryPath();
    }

}
