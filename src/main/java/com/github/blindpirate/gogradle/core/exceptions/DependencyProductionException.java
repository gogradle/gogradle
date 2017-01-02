package com.github.blindpirate.gogradle.core.exceptions;

import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency;

import java.io.File;
import java.io.IOException;

public class DependencyProductionException extends RuntimeException {
    private DependencyProductionException(Throwable cause) {
        super(cause);
    }

    private DependencyProductionException(String message, Throwable cause) {
        super(message, cause);
    }

    public static DependencyProductionException sourceCodeParsingFailed(File rootDir, IOException e) {
        return new DependencyProductionException(
                "Exception thrown when parsing source code in " + rootDir.getAbsolutePath(), e);
    }
    public static DependencyProductionException cannotResolveVendor(ResolvedDependency module, IOException e) {
        return new DependencyProductionException("Resolving vendor of " + module.getName() + " failed.", e);
    }
}
