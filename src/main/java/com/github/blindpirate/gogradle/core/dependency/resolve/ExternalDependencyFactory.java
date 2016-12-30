package com.github.blindpirate.gogradle.core.dependency.resolve;

import com.github.blindpirate.gogradle.core.GolangPackageModule;
import com.github.blindpirate.gogradle.core.InjectionHelper;
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet;
import com.github.blindpirate.gogradle.core.dependency.parse.MapNotationParser;
import com.github.blindpirate.gogradle.util.Assert;

import javax.inject.Inject;
import java.io.File;
import java.util.Map;
import java.util.Optional;

import java.nio.file.Path;
import java.util.List;

public abstract class ExternalDependencyFactory implements DependencyFactory {
    @Inject
    protected MapNotationParser mapNotationParser;

    /**
     * Relative paths of identity files.
     * For example, "Godeps/Godeps.json","glide.yaml"
     *
     * @return
     */
    protected abstract String identityFileName();

    @Override
    public Optional<GolangDependencySet> produce(GolangPackageModule module) {
        File identityFile = identityFile(module);
        if (identityFile.exists()) {
            List<Map<String, Object>> mapNotations = adapt(identityFile);
            return Optional.of(InjectionHelper.parseMany(mapNotations, mapNotationParser));
        } else {
            return Optional.empty();
        }
    }

    protected abstract List<Map<String, Object>> adapt(File file);

    private File identityFile(GolangPackageModule module) {
        String identityFile = identityFileName();
        Assert.isNotBlank(identityFile, "Identity file must not be empty!");
        Path rootDir = module.getRootDir();
        return rootDir.resolve(identityFile).toFile();
    }
}
