package com.github.blindpirate.gogradle.core.exceptions;

public final class PackageResolutionException extends RuntimeException {
    private PackageResolutionException(String message) {
        super(message);
    }

    public static PackageResolutionException cannotResolvePath(String packagePath) {
        return new PackageResolutionException("Cannot produce package:" + packagePath);
    }
}
