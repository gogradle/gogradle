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

package com.github.blindpirate.gogradle.core.cache;

import com.github.blindpirate.gogradle.core.dependency.NotationDependency;
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency;
import groovy.lang.Singleton;
import org.gradle.api.Project;

import javax.inject.Inject;
import java.io.File;

@Singleton
public class PersistenceNotationToResolvedCache
        extends PersistentCache<NotationDependency, ResolvedDependency> {

    @Inject
    public PersistenceNotationToResolvedCache(Project project) {
        super(new File(project.getRootDir(), ".gogradle/cache/PersistenceNotationToResolvedCache.bin"));
    }
}
