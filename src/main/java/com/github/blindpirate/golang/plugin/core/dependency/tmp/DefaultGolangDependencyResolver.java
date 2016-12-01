package com.github.blindpirate.golang.plugin.core.dependency.tmp;

import com.github.blindpirate.golang.plugin.core.GolangPackageModule;
import com.github.blindpirate.golang.plugin.core.dependency.GolangPackageDependency;
import org.gradle.api.file.SourceDirectorySet;

import java.util.List;
import java.util.Set;

public class DefaultGolangDependencyResolver implements DependencyResolver {
    private DependencyFetcher fetcher;
    private List<DependencyFactory> dependencyFactories;

    @Override
    public Set<GolangPackageDependency> resolve(GolangPackageModule module) {

        SourceDirectorySet sourceDir = fetcher.getDirectoryFiles(module);

        return null;
    }
}
