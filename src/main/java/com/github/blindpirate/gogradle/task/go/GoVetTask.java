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

package com.github.blindpirate.gogradle.task.go;

import com.github.blindpirate.gogradle.Go;
import com.github.blindpirate.gogradle.GolangPluginSetting;

import javax.inject.Inject;

import static com.github.blindpirate.gogradle.task.GolangTaskContainer.VENDOR_TASK_NAME;
import static java.util.Arrays.asList;

public class GoVetTask extends Go {
    @Inject
    private GolangPluginSetting setting;

    public GoVetTask() {
        dependsOn(VENDOR_TASK_NAME);
    }

    protected void doAddDefaultAction() {
        doLast(task -> go(asList("vet", setting.getPackagePath() + "/...")));
    }
}
