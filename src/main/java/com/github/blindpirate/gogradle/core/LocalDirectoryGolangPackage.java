package com.github.blindpirate.gogradle.core;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class LocalDirectoryGolangPackage extends ResolvableGolangPackage {

    private String dir;

    private LocalDirectoryGolangPackage(Path rootPath, Path path, String dir) {
        super(rootPath, path);
        this.dir = dir;
    }

    public String getDir() {
        return dir;
    }

    @Override
    protected Optional<GolangPackage> longerPath(Path packagePath) {
        return Optional.of(of(getRootPathString(), getPathString(), dir));
    }

    @Override
    protected Optional<GolangPackage> shorterPath(Path packagePath) {
        return Optional.of(IncompleteGolangPackage.of(packagePath));
    }

    public static LocalDirectoryGolangPackage of(Path rootPath, Path path, String dir) {
        return new LocalDirectoryGolangPackage(rootPath, path, dir);
    }

    public static LocalDirectoryGolangPackage of(String rootPath, String path, String dir) {
        return new LocalDirectoryGolangPackage(Paths.get(rootPath), Paths.get(path), dir);
    }
}
