package com.github.blindpirate.gogradle.core;

import com.github.blindpirate.gogradle.util.Assert;
import com.github.blindpirate.gogradle.vcs.VcsType;

import java.util.Optional;

public abstract class GolangPackage {
    private String path;

    public GolangPackage(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public abstract String getRootPath();

    public abstract VcsType getVcsType();

    public abstract String getUrl();

    public Optional<GolangPackage> resolve(String packagePath) {
        Assert.isTrue(packagePath.startsWith(path) || path.startsWith(packagePath));
        if (path.equals(packagePath)) {
            return Optional.of(this);
        } else if (path.startsWith(packagePath)) {
            return shorterPath(packagePath);
        } else {
            return longerPath(packagePath);
        }
    }

    protected abstract Optional<GolangPackage> longerPath(String packagePath);

    protected abstract Optional<GolangPackage> shorterPath(String packagePath);


}
