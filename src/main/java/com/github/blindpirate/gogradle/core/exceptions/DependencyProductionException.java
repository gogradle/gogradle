package com.github.blindpirate.gogradle.core.exceptions;

public class DependencyProductionException extends RuntimeException {
    private DependencyProductionException(String message) {
        super(message);
    }

    public static DependencyProductionException cannotRecognizePackage(String importPath) {
        return new DependencyProductionException("Cannot recognized package:" + importPath);
    }
}
