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

package com.github.blindpirate.gogradle;

import com.github.blindpirate.gogradle.core.GolangRepositoryPattern;
import groovy.lang.Closure;
import groovy.lang.GroovyObjectSupport;
import org.gradle.util.Configurable;
import org.gradle.util.ConfigureUtil;

import javax.annotation.Nullable;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Handles custom repositories. A {@code GolangRepositoryHandler} instance is registered into
 * {@code Project.getRepositories}, so the following code:
 * <pre>
 *     {@code
 *      repositories{
 *          golang {
 *              root 'github.com/some/package'
 *              dir '/path/to/the/package'
 *          }
 *
 *          golang {
 *              root 'appengine'
 *              emptyDir()
 *          }
 *
 *          golang {
 *              incomplete ~/gitlab\.com(\/.*)?/
 *          }
 *      }
 * }
 * </pre>
 * <p>
 * will result in two invocations of {@code GolangRepositoryHandler.configure} method.
 */
@Singleton
public class GolangRepositoryHandler extends GroovyObjectSupport implements Configurable<Void> {

    private List<GolangRepositoryPattern> golangRepositories = new ArrayList<>();

    public Optional<GolangRepositoryPattern> findMatchedRepository(String name) {
        return golangRepositories.stream()
                .filter(repo -> repo.match(name))
                .findFirst();
    }

    @Override
    @Nullable
    public Void configure(Closure cl) {
        GolangRepositoryPattern repository = new GolangRepositoryPattern();
        ConfigureUtil.configure(cl, repository);
        golangRepositories.add(repository);
        return null;
    }

    public void addEmptyRepo(String repo) {
        GolangRepositoryPattern repositoryPattern = new GolangRepositoryPattern();
        repositoryPattern.root(repo);
        repositoryPattern.emptyDir();
        golangRepositories.add(repositoryPattern);
    }
}
