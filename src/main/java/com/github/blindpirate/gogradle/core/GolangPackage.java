package com.github.blindpirate.gogradle.core;

import com.github.blindpirate.gogradle.util.Assert;
import com.github.blindpirate.gogradle.util.StringUtils;

import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;

public abstract class GolangPackage implements Serializable {
    // java.io.NotSerializableException: sun.nio.fs.UnixPath
    private String pathString;

    public GolangPackage(Path path) {
        this.pathString = StringUtils.toUnixString(path);
    }

    public Path getPath() {
        return Paths.get(pathString);
    }

    public String getPathString() {
        return pathString;
    }

    public Optional<GolangPackage> resolve(Path packagePath) {
        Path path = getPath();
        Assert.isTrue(packagePath.startsWith(path) || path.startsWith(packagePath));
        if (path.equals(packagePath)) {
            return Optional.of(this);
        } else if (path.startsWith(packagePath)) {
            return shorterPath(packagePath);
        } else {
            return longerPath(packagePath);
        }
    }

    public Optional<GolangPackage> resolve(String packagePath) {
        return resolve(Paths.get(packagePath));
    }

    protected abstract Optional<GolangPackage> longerPath(Path packagePath);

    protected abstract Optional<GolangPackage> shorterPath(Path packagePath);

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GolangPackage that = (GolangPackage) o;
        return Objects.equals(pathString, that.pathString);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pathString);
    }
}
