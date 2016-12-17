package com.github.blindpirate.gogradle.core.dependency.resolve;

import com.github.blindpirate.gogradle.core.GolangPackageModule;
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet;
import com.github.blindpirate.gogradle.util.Assert;
import com.google.common.base.Optional;

import java.nio.file.Path;
import java.util.List;

import static com.github.blindpirate.gogradle.util.CollectionUtils.collectOptional;

public abstract class ExternalDependencyFactory implements DependencyFactory {

    /**
     * Relative paths of identity files.
     * For example, "Godeps/Godeps.json","glide.yaml"
     *
     * @return
     */
    protected abstract List<String> identityFiles();

    public abstract Optional<GolangDependencySet> produceDeclaredDependencies(GolangPackageModule module);

    public abstract Optional<GolangDependencySet> produceLockedDependencies(GolangPackageModule module);

    /**
     * By default, it will fetch declared dependencies and locked dependencies, then merge it.
     * <p>
     * In general, locked dependencies will overwrite declared ones.
     *
     * @param module
     * @return
     */
    @Override
    public GolangDependencySet produce(GolangPackageModule module) {
        Optional<GolangDependencySet> declared = produceDeclaredDependencies(module);
        Optional<GolangDependencySet> locked = produceLockedDependencies(module);
        List<GolangDependencySet> sets = collectOptional(declared, locked);
        return GolangDependencySet.merge(sets);
    }

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
