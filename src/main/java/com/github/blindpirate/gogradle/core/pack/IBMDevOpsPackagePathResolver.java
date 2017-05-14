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

package com.github.blindpirate.gogradle.core.pack;

import com.github.blindpirate.gogradle.core.GolangPackage;
import com.github.blindpirate.gogradle.core.VcsGolangPackage;
import com.github.blindpirate.gogradle.vcs.VcsType;

import javax.inject.Singleton;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static com.github.blindpirate.gogradle.util.StringUtils.toUnixString;
import static java.util.Collections.singletonList;

@Singleton
public class IBMDevOpsPackagePathResolver extends AbstractPackagePathResolver {
    private static final String HUB_JAZZ_HOST = "hub.jazz.net";

    @Override
    protected Optional<GolangPackage> doProduce(String packagePath) {
        Path path = Paths.get(packagePath);
        Path rootPath = path.subpath(0, 4);
        GolangPackage pkg = VcsGolangPackage.builder()
                .withPath(path)
                .withRootPath(rootPath)
                .withOriginalVcsInfo(VcsType.GIT, singletonList(HTTPS + toUnixString(rootPath)))
                .build();
        return Optional.of(pkg);
    }

    @Override
    protected boolean isIncomplete(String packagePath) {
        return Paths.get(packagePath).getNameCount() < 4;
    }

    @Override
    protected boolean cannotRecognize(String packagePath) {
        return !HUB_JAZZ_HOST.equals(Paths.get(packagePath).getName(0).toString());
    }
}
