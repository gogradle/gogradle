package com.github.blindpirate.gogradle.task;

import com.github.blindpirate.gogradle.GolangPluginSetting;
import com.github.blindpirate.gogradle.build.BuildManager;
import com.github.blindpirate.gogradle.core.cache.GlobalCacheManager;
import com.github.blindpirate.gogradle.crossplatform.GoBinaryManager;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;

/**
 * This task perform preparation such as Go executable and GOPATH.
 */
public class PrepareTask extends DefaultTask {

    @Inject
    private GoBinaryManager goBinaryManager;

    @Inject
    private GlobalCacheManager globalCacheManager;

    @Inject
    private GolangPluginSetting setting;

    @Inject
    private BuildManager buildManager;

    @TaskAction
    public void prepare() {
        buildManager.prepareForBuild();
    }

}
