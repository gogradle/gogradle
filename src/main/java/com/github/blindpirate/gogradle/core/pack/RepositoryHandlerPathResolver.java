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

import com.github.blindpirate.gogradle.GolangRepositoryHandler;
import com.github.blindpirate.gogradle.core.GolangPackage;
import com.github.blindpirate.gogradle.core.LocalDirectoryGolangPackage;
import com.github.blindpirate.gogradle.core.VcsGolangPackage;
import com.github.blindpirate.gogradle.util.Assert;
import com.github.blindpirate.gogradle.util.StringUtils;
import com.github.blindpirate.gogradle.vcs.VcsType;
import com.github.blindpirate.gogradle.vcs.git.GolangRepository;
import groovy.lang.Singleton;

import javax.inject.Inject;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static com.github.blindpirate.gogradle.util.StringUtils.toUnixString;
import static java.util.Collections.singletonList;

@Singleton
public class RepositoryHandlerPathResolver implements PackagePathResolver {
    private final GolangRepositoryHandler repositoryHandler;

    @Inject
    public RepositoryHandlerPathResolver(GolangRepositoryHandler repositoryHandler) {
        this.repositoryHandler = repositoryHandler;
    }

    @Override
    public Optional<GolangPackage> produce(String packagePath) {
        Path path = Paths.get(packagePath);

        for (int i = path.getNameCount(); i > 0; i--) {
            Path subpath = path.subpath(0, i);
            GolangRepository repository = repositoryHandler.findMatchedRepository(toUnixString(subpath));
            if (repository != GolangRepository.EMPTY_INSTANCE) {
                return Optional.of(buildPackage(path, subpath, repository));
            }
        }

        return Optional.empty();
    }

    private GolangPackage buildPackage(Path path, Path rootPath, GolangRepository repository) {
        String rootPathString = StringUtils.toUnixString(rootPath);
        VcsType vcsType = repository.getVcsType();
        String url = repository.getUrl(rootPathString);
        String dir = repository.getDir(rootPathString);

        Assert.isTrue(url != null || dir != null, "You must specify dir or url for " + rootPathString);

        if (url != null) {
            return VcsGolangPackage.builder()
                    .withPath(path)
                    .withRootPath(rootPath)
                    .withSubstitutedVcsInfo(vcsType, singletonList(url))
                    .build();
        } else {
            return LocalDirectoryGolangPackage.of(rootPath, path, dir);
        }
    }
}
