package com.github.blindpirate.gogradle.core.cache;

import com.github.blindpirate.gogradle.core.GolangPackage;
import com.github.blindpirate.gogradle.core.dependency.GolangDependency;
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet;
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency;
import com.github.blindpirate.gogradle.core.pack.PackagePathResolver;
import groovy.lang.Singleton;
import org.gradle.api.Project;

import javax.inject.Inject;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Map.Entry;

@Singleton
public class PersistenceResolvedToDependenciesCache
        extends PersistentCache<ResolvedDependency, GolangDependencySet> {

    private final PackagePathResolver packagePathResolver;

    @Inject
    public PersistenceResolvedToDependenciesCache(Project project, PackagePathResolver packagePathResolver) {
        super(new File(project.getRootDir(), ".gogradle/cache/PersistenceResolvedToDependenciesCache.bin"));
        this.packagePathResolver = packagePathResolver;
    }

    public void load() {
        super.load();
        cleanseCacheData();
    }

    private void cleanseCacheData() {
        container = container.entrySet().stream().filter(this::shouldBePreserved)
                .collect(Collectors.toConcurrentMap(Entry::getKey, Entry::getValue));
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
