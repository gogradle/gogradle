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
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;

import static com.github.blindpirate.gogradle.task.GolangTaskContainer.CLEAN_TASK_NAME;

/**
 * This task perform preparation such as Go executable and GOPATH.
 */
public class GoPrepare extends DefaultTask {
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

    public GoPrepare() {
        shouldRunAfter(CLEAN_TASK_NAME);
    }

    @TaskAction
    public void prepare() {
        setting.verify();
        goBinaryManager.getBinaryPath();
        buildManager.createProjectLayoutIfNotExist();
        buildManager.prepareProjectGopathIfNecessary();
        buildConstraintManager.prepareConstraints();
        gogradleRootProject.setName(setting.getPackagePath());
    }
}
