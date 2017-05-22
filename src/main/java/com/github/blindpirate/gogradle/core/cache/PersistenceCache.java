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

import com.github.blindpirate.gogradle.core.GolangCloneable;
import org.gradle.api.Project;

import java.io.File;

public class PersistenceCache<K extends GolangCloneable, V extends GolangCloneable>
        extends CloneBackedCache<K, V> {
    private final File persistenceFile;

    public PersistenceCache(Project project, String persistenceFileName) {
        this.persistenceFile = new File(project.getRootDir(), ".gogradle/cache/" + persistenceFileName);
    }

    @SuppressWarnings("unchecked")
    public void load() {
        PersistenceCacheHelper.load(container, persistenceFile);
    }

    public void save() {
        PersistenceCacheHelper.save(container, persistenceFile);
    }
}
