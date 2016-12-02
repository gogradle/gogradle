package com.github.blindpirate.gogradle.core.dependency;

import com.github.blindpirate.gogradle.core.GolangPackageModule;

import java.util.List;

public abstract class AbstractDependencyFactory implements DependencyFactory {

    protected abstract List<String> identityFiles();

    @Override
    public boolean accept(GolangPackageModule module) {
        return anyFileExist();
    }

    private boolean anyFileExist() {
        return true;
    }
}
