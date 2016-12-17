package com.github.blindpirate.gogradle.core.dependency.resolve;

import com.github.blindpirate.gogradle.core.GolangPackageModule;
import com.github.blindpirate.gogradle.util.Assert;

import java.nio.file.Path;
import java.util.List;

public abstract class ExternalDependencyFactory implements DependencyFactory {

    /**
     * Relative paths of identity files.
     * For example, "Godeps/Godeps.json","glide.yaml"
     *
     * @return
     */
    protected abstract List<String> identityFiles();

    @Override
    public boolean accept(GolangPackageModule module) {
        return anyFileExist(module);
    }

    private boolean anyFileExist(GolangPackageModule module) {
        List<String> identityFiles = identityFiles();
        Assert.isNotEmpty(identityFiles, "Identity files must not be empty!");
        Path rootDir = module.getRootDir();

        for (String file : identityFiles) {
            if (rootDir.resolve(file).toFile().exists()) {
                return true;
            }
        }

        return false;
    }
}
