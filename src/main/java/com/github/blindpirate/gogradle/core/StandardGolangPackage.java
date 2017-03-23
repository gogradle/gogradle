package com.github.blindpirate.gogradle.core;

import java.nio.file.Path;
import java.util.Optional;

public class StandardGolangPackage extends GolangPackage {

    private StandardGolangPackage(Path path) {
        super(path);
    }

    public Path getRootPath() {
        return getPath();
    }

    @Override
    protected Optional<GolangPackage> longerPath(Path packagePath) {
        return Optional.of(of(packagePath));
    }

    @Override
    protected Optional<GolangPackage> shorterPath(Path packagePath) {
        return Optional.of(of(packagePath));
    }

    public static StandardGolangPackage of(Path path) {
        return new StandardGolangPackage(path);
    }

    @Override
    public String toString() {
        return "StandardGolangPackage{"
                + "path='" + getPathString() + '\''
                + '}';
    }
}
