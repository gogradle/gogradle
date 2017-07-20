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

package com.github.blindpirate.gogradle.ide;

import com.github.blindpirate.gogradle.task.AbstractGolangTask;
import com.google.inject.Inject;
import org.gradle.api.tasks.TaskAction;

import static com.github.blindpirate.gogradle.task.GolangTaskContainer.VENDOR_TASK_NAME;

public class GoglandTask extends AbstractGolangTask {
    @Inject
    private GoglandIntegration goglandIntegration;

    public GoglandTask() {
        setGroup("IDE");
        setDescription("Generate project configurations for gogland (https://www.jetbrains.com/go/).");
        dependsOn(VENDOR_TASK_NAME);
    }

    @TaskAction
    public void generateXmls() {
        goglandIntegration.generateXmls();
    }
}
