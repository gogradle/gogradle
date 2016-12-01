package com.github.blindpirate.golang.plugin.core.dependency.tmp;

import com.github.blindpirate.golang.plugin.core.GolangPackage;
import com.github.blindpirate.golang.plugin.core.dependency.GolangPackageDependency;
import org.gradle.api.file.SourceDirectorySet;

public interface DependencyFetcher {
    SourceDirectorySet getDirectoryFiles(GolangPackage golangPackage);
}
