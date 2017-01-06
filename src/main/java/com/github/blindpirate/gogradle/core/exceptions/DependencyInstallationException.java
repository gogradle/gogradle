package com.github.blindpirate.gogradle.core.exceptions;

import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency;

public class DependencyInstallationException extends RuntimeException {
    public DependencyInstallationException(String message) {
        super(message);
    }

    public DependencyInstallationException(String message, Exception cause) {
        super(message, cause);
    }

    public static DependencyInstallationException cannotResetResolvedDependency(ResolvedDependency dependency,
                                                                                Exception e) {
        return new DependencyInstallationException("Cannot reset dependency: " + dependency, e);
    }

}
