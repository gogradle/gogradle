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

import com.github.blindpirate.gogradle.build.BuildManager;
import com.github.blindpirate.gogradle.crossplatform.GoBinaryManager;
import com.google.inject.Inject;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.TaskAction;

import static com.github.blindpirate.gogradle.task.GolangTaskContainer.PREPARE_TASK_NAME;
import static com.github.blindpirate.gogradle.util.StringUtils.toUnixString;

public class ShowGopathGorootTask extends AbstractGolangTask {
    private static final Logger LOGGER = Logging.getLogger(ShowGopathGorootTask.class);

    @Inject
    private GoBinaryManager goBinaryManager;
    @Inject
    private BuildManager buildManager;

    public ShowGopathGorootTask() {
        dependsOn(PREPARE_TASK_NAME);
    }

    @TaskAction
    public void showGopathGoroot() {
        LOGGER.quiet("GOPATH: {}", buildManager.getGopath());
        LOGGER.quiet("GOROOT: {}", toUnixString(goBinaryManager.getGoroot()));
    }
}
