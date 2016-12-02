package com.github.blindpirate.gogradle.core;

import com.github.blindpirate.gogradle.core.dependency.GolangPackageDependency;
import org.gradle.api.file.ConfigurableFileTree;

import java.util.Set;

public class ResolvablePackageModule implements GolangPackageModule {
    @Override
    public String getName() {
        return null;
    }

    @Override
    public Set<GolangPackageDependency> getDependencies() {
        return null;
    }

    @Override
    public ConfigurableFileTree getRootDir() {
        return null;
    }
}
