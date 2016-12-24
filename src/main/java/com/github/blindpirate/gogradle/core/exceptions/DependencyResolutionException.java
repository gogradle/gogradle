package com.github.blindpirate.gogradle.core.exceptions;

import com.github.blindpirate.gogradle.core.GolangPackageModule;
import com.github.blindpirate.gogradle.core.dependency.GolangDependency;
import org.gradle.api.GradleException;

import java.io.IOException;

public class DependencyResolutionException extends GradleException {
    private DependencyResolutionException() {
    }

    private DependencyResolutionException(String message) {
        super(message);
    }

    private DependencyResolutionException(String message, Throwable cause) {
        super(message, cause);
    }

    private DependencyResolutionException(Throwable e) {
        this("Dependency resolution failed, the cause is:" + e.getMessage());
    }

    public static DependencyResolutionException sourceCodeParsingFailed(GolangPackageModule module, IOException e) {
        return new DependencyResolutionException("Parsing source code of " + module.getName() + " failed.", e);
    }

    public static DependencyResolutionException cannotResolveVendor(GolangPackageModule module, IOException e) {
        return new DependencyResolutionException("Resolving vendor of " + module.getName() + " failed.", e);
    }

    public static DependencyResolutionException cannotCloneRepository(GolangDependency dependency, Throwable e) {
        return new DependencyResolutionException("Cloning repository of " + dependency.getName() + " failed.", e);
    }
}
