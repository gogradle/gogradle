/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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
import java.util.List;

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
        if (goLockExistsInCurrentTasks() && gogradleDotLock.exists()) {
            LOGGER.warn("gogradle.lock already exists, it will be removed now.");
            IOUtils.forceDelete(gogradleDotLock);
        }
    }

    private boolean goLockExistsInCurrentTasks() {
        List<String> taskNames = getProject().getGradle().getStartParameter().getTaskNames();
        return taskNames.contains(GolangTaskContainer.LOCK_TASK_NAME)
                || taskNames.contains("gL");
    }

}
