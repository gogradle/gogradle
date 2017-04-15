package com.github.blindpirate.gogradle.task;

import com.github.blindpirate.gogradle.GolangPluginSetting;
import com.github.blindpirate.gogradle.build.BuildManager;
import com.github.blindpirate.gogradle.core.BuildConstraintManager;
import com.github.blindpirate.gogradle.core.cache.ProjectCacheManager;
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
    private ProjectCacheManager projectCacheManager;

    @Inject
    private BuildManager buildManager;

    @Inject
    private BuildConstraintManager buildConstraintManager;

    @Inject
    private GolangPluginSetting setting;

    @TaskAction
    public void prepare() {
        setting.verify();
        goBinaryManager.getBinaryPath();
        buildManager.ensureDotVendorDirNotExist();
        buildManager.prepareSymbolicLinks();
        projectCacheManager.loadPersistenceCache();
        buildConstraintManager.prepareConstraints();
    }

}
