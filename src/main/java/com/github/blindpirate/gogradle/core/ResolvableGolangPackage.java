package com.github.blindpirate.gogradle.core;

import com.github.blindpirate.gogradle.util.StringUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public abstract class ResolvableGolangPackage extends GolangPackage {
    private String rootPathString;

    protected ResolvableGolangPackage(Path rootPath, Path path) {
        super(path);
        this.rootPathString = StringUtils.toUnixString(rootPath);
    }

    public String getRootPathString() {
        return rootPathString;
    }

    public Path getRootPath() {
        return Paths.get(rootPathString);
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) {
            return false;
        }
        ResolvableGolangPackage that = (ResolvableGolangPackage) o;
        return Objects.equals(rootPathString, that.rootPathString);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), rootPathString);
    }
}
