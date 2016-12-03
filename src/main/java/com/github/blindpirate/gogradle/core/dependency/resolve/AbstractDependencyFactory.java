package com.github.blindpirate.gogradle.core.dependency.resolve;

import com.github.blindpirate.gogradle.core.GolangPackageModule;
import com.github.blindpirate.gogradle.core.dependency.resolve.DependencyFactory;

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
