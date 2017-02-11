package com.github.blindpirate.gogradle.core;

import com.github.blindpirate.gogradle.vcs.VcsType;

import java.util.List;
import java.util.Optional;

public class StandardGolangPackage extends GolangPackage {

    private StandardGolangPackage(String path) {
        super(path);
    }

    @Override
    public String getRootPath() {
        return getPath();
    }

    @Override
    public VcsType getVcsType() {
        throw new UnsupportedOperationException(toString());
    }

    @Override
    public List<String> getUrls() {
        throw new UnsupportedOperationException(toString());
    }

    @Override
    protected Optional<GolangPackage> longerPath(String packagePath) {
        return Optional.of(of(packagePath));
    }

    @Override
    protected Optional<GolangPackage> shorterPath(String packagePath) {
        return Optional.of(of(packagePath));
    }

    public static StandardGolangPackage of(String path) {
        return new StandardGolangPackage(path);
    }

    @Override
    public String toString() {
        return "StandardGolangPackage{"
                + "path='" + getPath() + '\''
                + '}';
    }
}
