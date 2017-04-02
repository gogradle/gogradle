package com.github.blindpirate.gogradle.core;

import com.github.blindpirate.gogradle.util.StringUtils;

import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class ResolvableGolangPackage extends GolangPackage {
    private String rootPathString;

    protected ResolvableGolangPackage(Path rootPath, Path path) {
        super(path);
        this.rootPathString = StringUtils.toUnixString(rootPath);
    }

    public boolean isRoot() {
        return rootPathString.equals(getPathString());
    }

    public String getRootPathString() {
        return rootPathString;
    }

    public Path getRootPath() {
        return Paths.get(rootPathString);
    }
}
