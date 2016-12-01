package com.github.blindpirate.golang.plugin.core.dependency.tmp;

import com.github.blindpirate.golang.plugin.core.dependency.GolangPackageDependency;
import org.gradle.api.file.DirectoryTree;
import org.gradle.api.file.FileTree;
import org.gradle.api.file.SourceDirectorySet;

import java.util.Set;

public interface DependencyFactory extends PickyFactory<DirectoryTree, Set<GolangPackageDependency>> {
}
