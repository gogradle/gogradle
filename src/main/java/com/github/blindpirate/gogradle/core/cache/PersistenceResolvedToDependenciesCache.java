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

import com.github.blindpirate.gogradle.core.GolangPackage;
import com.github.blindpirate.gogradle.core.dependency.GolangDependency;
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet;
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency;
import com.github.blindpirate.gogradle.core.pack.PackagePathResolver;
import org.gradle.api.Project;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.github.blindpirate.gogradle.GogradleGlobal.GOGRADLE_COMPATIBLE_VERSION;


@Singleton
public class PersistenceResolvedToDependenciesCache
        extends PersistenceCache<ResolvedDependency, GolangDependencySet> {

    private final PackagePathResolver packagePathResolver;

    @Inject
    public PersistenceResolvedToDependenciesCache(Project project, PackagePathResolver packagePathResolver) {
        super(project, "PersistenceResolvedToDependenciesCache-"
                + GOGRADLE_COMPATIBLE_VERSION + ".bin");
        this.packagePathResolver = packagePathResolver;
    }

    @Override
    public void load() {
        super.load();
        removeCachedItemWhosePackageHasChanged();
    }

    private void removeCachedItemWhosePackageHasChanged() {
        List<Map.Entry> entriesToRemove = container.entrySet().stream()
                .filter(entry -> !shouldBePreserved(entry))
                .collect(Collectors.toList());
        entriesToRemove.forEach(entry -> container.remove(entry.getKey()));
    }

    private boolean shouldBePreserved(Map.Entry<ResolvedDependency, GolangDependencySet> entry) {
        List<GolangDependency> dependencies = entry.getValue().flatten();
        return dependencies.stream().allMatch(this::packageIsSame);
    }

    private boolean packageIsSame(GolangDependency dependency) {
        GolangPackage newPkg = packagePathResolver.produce(dependency.getName()).get();
        GolangPackage oldPkg = dependency.getPackage();
        return newPkg.equals(oldPkg);
    }
}
