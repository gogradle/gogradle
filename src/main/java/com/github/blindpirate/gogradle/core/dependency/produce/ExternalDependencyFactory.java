package com.github.blindpirate.gogradle.core.dependency.produce;

import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet;
import com.github.blindpirate.gogradle.core.dependency.parse.MapNotationParser;
import com.github.blindpirate.gogradle.core.pack.StandardPackagePathResolver;
import com.github.blindpirate.gogradle.util.Assert;
import com.github.blindpirate.gogradle.util.MapUtils;
import com.google.common.collect.ImmutableMap;

import javax.inject.Inject;
import java.io.File;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.github.blindpirate.gogradle.core.GolangConfiguration.BUILD;
import static com.github.blindpirate.gogradle.core.GolangConfiguration.TEST;
import static com.github.blindpirate.gogradle.util.DependencySetUtils.parseMany;

public abstract class ExternalDependencyFactory {
    private Map<String, Function<File, List>> adapters =
            ImmutableMap.of(BUILD, this::adapt, TEST, this::adaptTest);

    @Inject
    protected MapNotationParser mapNotationParser;

    @Inject
    private StandardPackagePathResolver standardPackagePathResolver;

    /**
     * Relative paths of the identity file.
     * For example, "Godeps/Godeps.json","glide.yaml"
     *
     * @return name of that file
     */
    public abstract String identityFileName();

    @SuppressWarnings("unchecked")
    public Optional<GolangDependencySet> produce(File rootDir, String configuration) {
        File identityFile = identityFile(rootDir);
        if (identityFile.exists()) {
            Function<File, List> adapter = adapters.get(configuration);
            List<Map<String, Object>> mapNotations = adapter.apply(identityFile);

            mapNotations = removeStandardPackages(mapNotations);

            return Optional.of(parseMany(mapNotations, mapNotationParser));
        } else {
            return Optional.empty();
        }
    }

    private List<Map<String, Object>> removeStandardPackages(List<Map<String, Object>> packages) {
        return packages.stream().filter(pkg -> {
            String path = MapUtils.getString(pkg, MapNotationParser.NAME_KEY, "");
            return !standardPackagePathResolver.isStandardPackage(Paths.get(path));

        }).collect(Collectors.toList());
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
        return new File(rootDir, identityFile);
    }
}
