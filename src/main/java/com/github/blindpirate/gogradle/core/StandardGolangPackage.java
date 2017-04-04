package com.github.blindpirate.gogradle.core;

import com.github.blindpirate.gogradle.util.StringUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class StandardGolangPackage extends GolangPackage {

    private StandardGolangPackage(Path path) {
        super(path);
    }

    public Path getRootPath() {
        return getPath();
    }

    public String getRootPathString() {
        return StringUtils.toUnixString(getPath());
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

    public static StandardGolangPackage of(String path) {
        return new StandardGolangPackage(Paths.get(path));
    }

    @Override
    public String toString() {
        return "StandardGolangPackage{"
                + "path='" + getPathString() + '\''
                + '}';
    }
}
