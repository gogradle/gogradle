package com.github.blindpirate.gogradle.core;

import com.github.blindpirate.gogradle.util.StringUtils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class LocalDirectoryGolangPackage extends GolangPackage {

    private String rootPathString;

    private File dir;

    private LocalDirectoryGolangPackage(Path rootPath, Path path, File dir) {
        super(path);
        rootPathString = StringUtils.toUnixString(rootPath);
        this.dir = dir;
    }

    public String getRootPathString() {
        return rootPathString;
    }

    @Override
    protected Optional<GolangPackage> longerPath(Path packagePath) {
        return Optional.of(of(rootPathString, getPathString(), dir));
    }

    @Override
    protected Optional<GolangPackage> shorterPath(Path packagePath) {
        return Optional.of(IncompleteGolangPackage.of(packagePath));
    }

    public static LocalDirectoryGolangPackage of(Path rootPath, Path path, File dir) {
        return new LocalDirectoryGolangPackage(rootPath, path, dir);
    }

    public static LocalDirectoryGolangPackage of(String rootPath, String path, File dir) {
        return new LocalDirectoryGolangPackage(Paths.get(rootPath), Paths.get(path), dir);
    }
}
