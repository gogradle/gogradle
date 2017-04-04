package com.github.blindpirate.gogradle.core;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class IncompleteGolangPackage extends GolangPackage {
    private IncompleteGolangPackage(Path path) {
        super(path);
    }

    @Override
    protected Optional<GolangPackage> longerPath(Path packagePath) {
        return Optional.empty();
    }

    @Override
    protected Optional<GolangPackage> shorterPath(Path packagePath) {
        return Optional.of(of(packagePath));
    }

    public static IncompleteGolangPackage of(Path path) {
        return new IncompleteGolangPackage(path);
    }

    public static IncompleteGolangPackage of(String path) {
        return of(Paths.get(path));
    }

    @Override
    public String toString() {
        return "IncompleteGolangPackage{"
                + "path='" + getPathString() + '\''
                + '}';
    }
}
