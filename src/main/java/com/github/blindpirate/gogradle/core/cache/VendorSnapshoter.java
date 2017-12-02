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

import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency;
import org.apache.commons.collections4.map.LRUMap;
import org.gradle.api.Project;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.util.Map;

import static com.github.blindpirate.gogradle.GogradleGlobal.GOGRADLE_COMPATIBLE_VERSION;
import static com.github.blindpirate.gogradle.core.cache.Cache.DEFAULT_LRU_CAPACITY;

@Singleton
public class VendorSnapshoter {
    private Map<ResolvedDependency, DirectorySnapshot> cache = new LRUMap<>(DEFAULT_LRU_CAPACITY);

    private final Project project;

    private final File persistenceFile;

    @Inject
    public VendorSnapshoter(Project project) {
        this.project = project;
        this.persistenceFile = new File(project.getProjectDir(),
                ".gogradle/cache/VendorSnapshot-" + GOGRADLE_COMPATIBLE_VERSION + ".bin");
    }

    public void loadPersistenceCache() {
        PersistenceCacheHelper.load(cache, persistenceFile);
    }

    public void savePersistenceCache() {
        PersistenceCacheHelper.save(cache, persistenceFile);
    }

    public boolean isUpToDate(ResolvedDependency resolvedDependency, File dir) {
        DirectorySnapshot lastInstallationSnapshot = cache.get(resolvedDependency);
        if (lastInstallationSnapshot == null) {
            return false;
        } else {
            return lastInstallationSnapshot.isUpToDate(project.getProjectDir(), dir);
        }
    }

    public void updateCache(ResolvedDependency resolvedDependency, File dir) {
        if (resolvedDependency.getCacheScope() == CacheScope.PERSISTENCE) {
            cache.put(resolvedDependency, DirectorySnapshot.of(project.getProjectDir(), dir));
        }
    }
}
