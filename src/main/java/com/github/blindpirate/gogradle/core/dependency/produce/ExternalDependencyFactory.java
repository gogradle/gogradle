package com.github.blindpirate.gogradle.core.dependency.produce;

import com.github.blindpirate.gogradle.build.Configuration;
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet;
import com.github.blindpirate.gogradle.core.dependency.parse.MapNotationParser;
import com.github.blindpirate.gogradle.util.Assert;
import com.github.blindpirate.gogradle.util.DependencySetUtils;
import com.google.common.collect.ImmutableMap;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static com.github.blindpirate.gogradle.build.Configuration.BUILD;
import static com.github.blindpirate.gogradle.build.Configuration.TEST;

public abstract class ExternalDependencyFactory {
    private Map<Configuration, Function<File, List>> adapters =
            ImmutableMap.of(BUILD, this::adapt, TEST, this::adaptTest);

    protected final MapNotationParser mapNotationParser;

    public ExternalDependencyFactory(MapNotationParser mapNotationParser) {
        this.mapNotationParser = mapNotationParser;
    }

    /**
     * Relative paths of the identity file.
     * For example, "Godeps/Godeps.json","glide.yaml"
     *
     * @return
     */
    protected abstract String identityFileName();

    @SuppressWarnings("unchecked")
    public Optional<GolangDependencySet> produce(File rootDir, Configuration configuration) {
        File identityFile = identityFile(rootDir);
        if (identityFile.exists()) {
            Function<File, List> adapter = adapters.get(configuration);
            List<Map<String, Object>> mapNotations = adapter.apply(identityFile);
            return Optional.of(DependencySetUtils.parseMany(mapNotations, mapNotationParser));
        } else {
            return Optional.empty();
        }
    }

    /**
     * In most cases, this method won't be used because all we need is build dependencies of
     * external package. However, when gogradle is building a golang project which was originally
     * managed by an external management tool, this method will be used to analyze test dependencies of the project.
     *
     * @param identityFile the identity file
     * @return test dependency of this identity file
     */
    protected List<Map<String, Object>> adaptTest(File identityFile) {
        return Collections.emptyList();
    }

    protected abstract List<Map<String, Object>> adapt(File file);

    private File identityFile(File rootDir) {
        String identityFile = identityFileName();
        Assert.isNotBlank(identityFile, "Identity file must not be empty!");
        return rootDir.toPath().resolve(identityFile).toFile();
    }
}
