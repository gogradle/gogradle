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
import java.util.Arrays;
import java.util.Optional;

import static com.github.blindpirate.gogradle.util.StringUtils.toUnixString;

// github.com/user/project -> git@github.com:user/project.git
// github.com/user/project -> https://github.com/user/project.git
public class GithubGitlabPackagePathResolver extends AbstractPackagePathResolver {

    private final String host;

    public GithubGitlabPackagePathResolver(String host) {
        this.host = host;
    }

    protected boolean isIncomplete(String packagePath) {
        return Paths.get(packagePath).getNameCount() < 3;
    }

    protected boolean cannotRecognize(String packagePath) {
        return !host.equals(Paths.get(packagePath).getName(0).toString().toLowerCase());
    }

    protected Optional<GolangPackage> doProduce(String packagePath) {
        Path path = Paths.get(packagePath);
        String sshUrl = String.format("git@%s.git", toUnixString(path.subpath(0, 3)).replaceFirst("/", ":"));
        String httpsUrl = String.format("https://%s.git", toUnixString(path.subpath(0, 3)));

        GolangPackage info = VcsGolangPackage.builder()
                .withPath(path)
                .withOriginalVcsInfo(VcsType.GIT, Arrays.asList(httpsUrl, sshUrl))
                .withRootPath(path.subpath(0, 3))
                .build();
        return Optional.of(info);
    }
}
