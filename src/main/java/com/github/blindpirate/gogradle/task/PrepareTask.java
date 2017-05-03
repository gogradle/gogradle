package com.github.blindpirate.gogradle.task;

import com.github.blindpirate.gogradle.GolangPluginSetting;
import com.github.blindpirate.gogradle.build.BuildManager;
import com.github.blindpirate.gogradle.core.BuildConstraintManager;
import com.github.blindpirate.gogradle.core.dependency.GogradleRootProject;
import com.github.blindpirate.gogradle.crossplatform.GoBinaryManager;
import com.github.blindpirate.gogradle.util.IOUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.io.File;

/**
 * This task perform preparation such as Go executable and GOPATH.
 */
public class PrepareTask extends DefaultTask {

    private static final Logger LOGGER = Logging.getLogger(PrepareTask.class);
    @Inject
    private GoBinaryManager goBinaryManager;

    @Inject
    private BuildManager buildManager;

    @Inject
    private BuildConstraintManager buildConstraintManager;

    @Inject
    private GolangPluginSetting setting;

    @Inject
    private GogradleRootProject gogradleRootProject;

    @TaskAction
    public void prepare() {
        setting.verify();
        goBinaryManager.getBinaryPath();
        buildManager.ensureDotVendorDirNotExist();
        buildManager.prepareSymbolicLinks();
        buildConstraintManager.prepareConstraints();
        gogradleRootProject.initSingleton(setting.getPackagePath(), getProject().getRootDir());
        deleteGogradleDotLockIfLockTaskExists();
    }

    private void deleteGogradleDotLockIfLockTaskExists() {
        File gogradleDotLock = new File(getProject().getRootDir(), "gogradle.lock");
        if (getProject().getTasks().getByName(GolangTaskContainer.LOCK_TASK_NAME) != null
                && gogradleDotLock.exists()) {
            LOGGER.warn("gogradle.lock already exists, it will be removed now.");
            IOUtils.forceDelete(gogradleDotLock);
        }
    }

}
