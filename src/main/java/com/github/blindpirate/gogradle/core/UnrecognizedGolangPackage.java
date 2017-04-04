package com.github.blindpirate.gogradle.core;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class UnrecognizedGolangPackage extends GolangPackage {

    private UnrecognizedGolangPackage(Path path) {
        super(path);
    }

    @Override
    protected Optional<GolangPackage> longerPath(Path packagePath) {
        // I cannot foresee the future
        // for example, I am `golang.org` and I am unrecognized
        // but `golang.org/x/tools` can be recognized
        return Optional.empty();
    }

    @Override
    protected Optional<GolangPackage> shorterPath(Path packagePath) {
        return Optional.of(of(packagePath));
    }

    public static UnrecognizedGolangPackage of(Path packagePath) {
        return new UnrecognizedGolangPackage(packagePath);
    }

    public static UnrecognizedGolangPackage of(String packagePath) {
        return of(Paths.get(packagePath));
    }

    @Override
    public String toString() {
        return "UnrecognizedGolangPackage{"
                + "path='" + getPathString() + '\''
                + '}';
    }
}
