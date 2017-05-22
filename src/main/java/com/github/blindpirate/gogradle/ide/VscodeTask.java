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

import com.github.blindpirate.gogradle.build.BuildManager;
import com.github.blindpirate.gogradle.task.AbstractGolangTask;
import com.github.blindpirate.gogradle.util.DataExchange;
import com.github.blindpirate.gogradle.util.IOUtils;
import com.google.inject.Inject;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.github.blindpirate.gogradle.task.GolangTaskContainer.VENDOR_TASK_NAME;

public class VscodeTask extends AbstractGolangTask {
    @Inject
    private BuildManager buildManager;

    public VscodeTask() {
        dependsOn(VENDOR_TASK_NAME);
    }

    @TaskAction
    public void addGopathToSettingsDotJson() {
        File settingsDotJson = new File(getProject().getRootDir(), ".vscode/settings.json");
        if (!settingsDotJson.exists()) {
            IOUtils.write(settingsDotJson, "{}");
        }

        String json = removeCommentLines(IOUtils.readLines(settingsDotJson));
        @SuppressWarnings("unchecked")
        Map<String, Object> model = DataExchange.parseJson(json, Map.class);
        // https://github.com/Microsoft/vscode-go/wiki/GOPATH-in-the-VS-Code-Go-extension
        model.put("go.gopath", buildManager.getGopath());
        IOUtils.write(settingsDotJson, DataExchange.toJson(model));
    }

    private String removeCommentLines(List<String> lines) {
        return lines.stream()
                .filter(line -> !line.trim().startsWith("//"))
                .collect(Collectors.joining("\n"));
    }
}
