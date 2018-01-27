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
import com.github.blindpirate.gogradle.core.dependency.produce.VendorDependencyFactory;
import com.github.blindpirate.gogradle.crossplatform.GoBinaryManager;
import com.github.blindpirate.gogradle.task.GolangTaskContainer;
import com.github.blindpirate.gogradle.util.CollectionUtils;
import com.github.blindpirate.gogradle.util.IOUtils;
import com.github.blindpirate.gogradle.util.StringUtils;

import javax.inject.Inject;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.blindpirate.gogradle.util.StringUtils.startsWithAny;
import static com.github.blindpirate.gogradle.util.StringUtils.toUnixString;

public class Gofmt extends Go {
    @Inject
    private GoBinaryManager goBinaryManager;

    public Gofmt() {
        setDescription("Run gofmt (https://golang.org/cmd/gofmt/).");
        dependsOn(GolangTaskContainer.PREPARE_TASK_NAME);
    }

    public void afterEvaluate() {
        // if user doesn't configure it
        if (CollectionUtils.isEmpty(goActions)) {
            run(CollectionUtils.asStringList(getGofmtPath(), "-w", children()));
        }
    }

    private String getGofmtPath() {
        Path goBinPath = goBinaryManager.getBinaryPath();
        Path gofmtPath = goBinPath.resolve("../gofmt").normalize();
        return toUnixString(gofmtPath.toAbsolutePath());
    }

    public void gofmt(String arg) {
        run(getGofmtPath() + " " + arg);
    }

    // A workaround because gofmt on Windows doesn't ignore .gogradle
    protected List<String> children() {
        return IOUtils.safeListFiles(getProject().getProjectDir())
                .stream()
                .filter(file -> !startsWithAny(file.getName(), "_", "."))
                .filter(file -> !VendorDependencyFactory.VENDOR_DIRECTORY.equals(file.getName()))
                .filter(file -> file.isDirectory() || file.getName().endsWith(".go"))
                .map(StringUtils::toUnixString)
                .collect(Collectors.toList());
    }
}
