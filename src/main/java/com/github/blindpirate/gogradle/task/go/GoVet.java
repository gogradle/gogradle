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
import com.github.blindpirate.gogradle.util.CollectionUtils;
import com.github.blindpirate.gogradle.util.StringUtils;
import org.apache.commons.io.FileUtils;


import javax.inject.Inject;
import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.blindpirate.gogradle.task.GolangTaskContainer.VENDOR_TASK_NAME;

public class GoVet extends Go {


    private static final String[] ALLOWED_EXTENSIONS = new String[]{"go"};
    @Inject
    private GolangPluginSetting setting;

    public GoVet() {
        setDescription("Run 'go vet' (https://golang.org/cmd/vet).");
        dependsOn(VENDOR_TASK_NAME);
    }

    @Override
    public void afterEvaluate() {
        if (CollectionUtils.isEmpty(goActions)) {
            vet(allSubGoFiles());
        }
    }

    private void vet(List<String> fileNames) {
        if (!fileNames.isEmpty()) {
            go(CollectionUtils.asStringList("vet", fileNames));
        }
    }

    private Path checkProjectSrcDir() {
        return getProjectDir().toPath().toAbsolutePath();
    }

    private List<String> allSubGoFiles() {
        Path path = checkProjectSrcDir();
        if (path.resolve("src").toFile().exists()) {
            return FileUtils.listFiles(path.resolve("src").toFile(),
                    ALLOWED_EXTENSIONS, true).stream()
                    .filter(File::isFile)
                    .filter(file -> !StringUtils.startsWithAny(file.getName(), "_", "."))
                    .filter(file -> StringUtils.endsWithAny(file.getName(), ".go"))
                    .map(StringUtils::toUnixString)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

}
