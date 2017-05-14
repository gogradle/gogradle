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

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.io.File;

import static com.github.blindpirate.gogradle.GogradleGlobal.GOGRADLE_BUILD_DIR_NAME;
import static com.github.blindpirate.gogradle.util.IOUtils.clearDirectory;
import static com.github.blindpirate.gogradle.util.IOUtils.isValidDirectory;

public class CleanTask extends DefaultTask {

    @TaskAction
    public void clean() {
        File gogradleBuildDir = new File(getProject().getRootDir(), GOGRADLE_BUILD_DIR_NAME);
        if (isValidDirectory(gogradleBuildDir)) {
            clearDirectory(gogradleBuildDir);
        }
    }
}
