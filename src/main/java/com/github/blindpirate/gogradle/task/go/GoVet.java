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
import com.github.blindpirate.gogradle.common.GoSourceCodeFilter;
import com.github.blindpirate.gogradle.crossplatform.GoBinaryManager;
import com.github.blindpirate.gogradle.util.CollectionUtils;
import com.github.blindpirate.gogradle.util.StringUtils;

import javax.inject.Inject;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.blindpirate.gogradle.common.GoSourceCodeFilter.SourceSetType.PROJECT_ALL_FILES_ONLY;
import static com.github.blindpirate.gogradle.task.GolangTaskContainer.VENDOR_TASK_NAME;
import static com.github.blindpirate.gogradle.util.CollectionUtils.asStringList;

public class GoVet extends Go {
    @Inject
    private GolangPluginSetting setting;
    @Inject
    private GoBinaryManager goBinaryManager;

    private boolean verbose;

    public GoVet() {
        setDescription("Run 'go vet' (https://golang.org/cmd/vet).");
        dependsOn(VENDOR_TASK_NAME);
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    @Override
    public void afterEvaluate() {
        if (CollectionUtils.isEmpty(goActions)) {
            defaultVet();
        }
    }

    private void defaultVet() {
        // Issue #287: `go tool vet` is deprecated in Go 1.12.
        // @see https://go-review.googlesource.com/c/go/+/152161/3/doc/go1.12.html
        goActions.add(new GoAction() {
            @Override
            public Integer get() {
                List<String> packagesToVet = getPackagesToVet();

                if (packagesToVet.isEmpty()) {
                    LOGGER.quiet("No valid packages found, skip.");
                    return 0;
                }
                List<String> vetCommands = verbose
                        ? asStringList("vet", "-v", packagesToVet)
                        : asStringList("vet", packagesToVet);
                return buildManager.go(vetCommands,
                        getEnvironment(),
                        getStdoutLineConsumer(),
                        getStderrLineConsumer(),
                        isContinueOnFailure());
            }
        });
    }

    private List<String> getPackagesToVet() {
        if (goBinaryManager.goVetIgnoreVendor() && !isProjectGopath()) {
            return Collections.singletonList(setting.getPackagePath() + "/...");
        } else {
            return new ArrayList<>(GoSourceCodeFilter.filterGoFiles(getProjectDir(), PROJECT_ALL_FILES_ONLY).stream()
                    .map(File::getParentFile)
                    .map(this::getPackagePath)
                    .collect(Collectors.toSet()));
        }
    }

    private String getPackagePath(File subDirPath) {
        if (subDirPath.equals(getProjectDir())) {
            return setting.getPackagePath();
        }
        Path relativePath = getProjectDir().toPath().relativize(subDirPath.toPath());
        return setting.getPackagePath() + "/" + StringUtils.toUnixString(relativePath);
    }

    private boolean isProjectGopath() {
        return buildManager.getGopath().contains(".gogradle/project_gopath");
    }
}
