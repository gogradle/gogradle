package com.github.blindpirate.gogradle.core.pack;

public final class PackageResolutionException extends RuntimeException {
    private PackageResolutionException(String message) {
        super(message);
    }

    public static PackageResolutionException cannotResolveName(String packageName) {
        return new PackageResolutionException("Cannot resolve package:" + packageName);
    }
}
