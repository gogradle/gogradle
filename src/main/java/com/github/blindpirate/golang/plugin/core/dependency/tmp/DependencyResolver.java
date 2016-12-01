package com.github.blindpirate.golang.plugin.core.dependency.tmp;

import com.github.blindpirate.golang.plugin.core.GolangPackageModule;
import com.github.blindpirate.golang.plugin.core.dependency.GolangPackageDependency;

import java.util.Set;

public interface DependencyResolver {
    Set<GolangPackageDependency> resolve(GolangPackageModule module);
}
