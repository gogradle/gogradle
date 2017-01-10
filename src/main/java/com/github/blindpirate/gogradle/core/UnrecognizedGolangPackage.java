package com.github.blindpirate.gogradle.core;

import com.github.blindpirate.gogradle.vcs.VcsType;

import java.util.Optional;

public class UnrecognizedGolangPackage extends GolangPackage {
    private UnrecognizedGolangPackage(String path) {
        super(path);
    }

    @Override
    public String getRootPath() {
        throw new UnsupportedOperationException();
    }

    @Override
    public VcsType getVcsType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getUrl() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected Optional<GolangPackage> longerPath(String packagePath) {
        // I cannot foresee the future
        // for example, I am `golang.org` and I am unrecognized
        // but `golang.org/x/tools` can be recognized
        return Optional.empty();
    }

    @Override
    protected Optional<GolangPackage> shorterPath(String packagePath) {
        return Optional.of(of(packagePath));
    }

    public static UnrecognizedGolangPackage of(String packagePath) {
        return new UnrecognizedGolangPackage(packagePath);
    }
}
