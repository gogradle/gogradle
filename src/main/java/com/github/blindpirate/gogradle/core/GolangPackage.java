package com.github.blindpirate.gogradle.core;

import com.github.blindpirate.gogradle.util.Assert;
import com.github.blindpirate.gogradle.util.StringUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public abstract class GolangPackage {
    private Path path;

    public GolangPackage(Path path) {
        this.path = path;
    }

    public Path getPath() {
        return path;
    }

    public String getPathString() {
        return StringUtils.toUnixString(path);
    }

    public Optional<GolangPackage> resolve(Path packagePath) {
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


}
