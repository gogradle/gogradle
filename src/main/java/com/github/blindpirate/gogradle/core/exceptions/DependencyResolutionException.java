package com.github.blindpirate.gogradle.core.exceptions;

import org.gradle.api.GradleException;

public class DependencyResolutionException extends GradleException {
    public DependencyResolutionException() {
    }

    public DependencyResolutionException(String message) {
        super(message);
    }

    public DependencyResolutionException(String message, Throwable cause) {
        super(message, cause);
    }

    public DependencyResolutionException(Throwable e) {
        this("Dependency resolution failed, the cause is:" + e.getMessage());
    }
}
