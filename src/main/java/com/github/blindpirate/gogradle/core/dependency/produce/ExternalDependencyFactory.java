package com.github.blindpirate.gogradle.core.dependency.produce;

import com.github.blindpirate.gogradle.core.InjectionHelper;
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet;
import com.github.blindpirate.gogradle.core.dependency.parse.MapNotationParser;
import com.github.blindpirate.gogradle.util.Assert;

import javax.inject.Inject;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class ExternalDependencyFactory {
    protected final MapNotationParser mapNotationParser;

    public ExternalDependencyFactory(MapNotationParser mapNotationParser) {
        this.mapNotationParser = mapNotationParser;
    }

    /**
     * Relative paths of identity files.
     * For example, "Godeps/Godeps.json","glide.yaml"
     *
     * @return
     */
    protected abstract String identityFileName();

    public Optional<GolangDependencySet> produce(File rootDir) {
        File identityFile = identityFile(rootDir);
        if (identityFile.exists()) {
            List<Map<String, Object>> mapNotations = adapt(identityFile);
            return Optional.of(InjectionHelper.parseMany(mapNotations, mapNotationParser));
        } else {
            return Optional.empty();
        }
    }

    protected abstract List<Map<String, Object>> adapt(File file);

    private File identityFile(File rootDir) {
        String identityFile = identityFileName();
        Assert.isNotBlank(identityFile, "Identity file must not be empty!");
        return rootDir.toPath().resolve(identityFile).toFile();
    }
}
