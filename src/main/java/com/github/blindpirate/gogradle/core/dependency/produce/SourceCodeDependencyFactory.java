package com.github.blindpirate.gogradle.core.dependency.produce;

import com.github.blindpirate.gogradle.build.Configuration;
import com.github.blindpirate.gogradle.core.GolangPackage;
import com.github.blindpirate.gogradle.core.StandardGolangPackage;
import com.github.blindpirate.gogradle.core.UnrecognizedGolangPackage;
import com.github.blindpirate.gogradle.core.dependency.GolangDependency;
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet;
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency;
import com.github.blindpirate.gogradle.core.dependency.parse.NotationParser;
import com.github.blindpirate.gogradle.core.exceptions.DependencyProductionException;
import com.github.blindpirate.gogradle.core.pack.PackagePathResolver;
import com.github.blindpirate.gogradle.util.IOUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.github.blindpirate.gogradle.util.StringUtils.isBlank;

/**
 * Scans all .go code to generate dependencies.
 */
@Singleton
public class SourceCodeDependencyFactory {

    public static final String TESTDATA_DIRECTORY = "testdata";

    private final GoImportExtractor goImportExtractor;
    private final PackagePathResolver packagePathResolver;
    private final NotationParser notationParser;

    @Inject
    public SourceCodeDependencyFactory(PackagePathResolver packagePathResolver,
                                       NotationParser notationParser,
                                       GoImportExtractor extractor) {
        this.packagePathResolver = packagePathResolver;
        this.notationParser = notationParser;
        this.goImportExtractor = extractor;
    }

    public GolangDependencySet produce(ResolvedDependency resolvedDependency,
                                       File rootDir,
                                       Configuration configuration) {
        SourceCodeDirectoryVisitor visitor = new SourceCodeDirectoryVisitor(configuration, goImportExtractor);
        IOUtils.walkFileTreeSafely(rootDir.toPath(), visitor);
        return createDependencies(resolvedDependency, visitor.getImportPaths());
    }

    private GolangDependencySet createDependencies(ResolvedDependency resolvedDependency, Set<String> importPaths) {
        Set<String> rootPackagePaths =
                importPaths.stream()
                        .filter(path -> !path.startsWith(resolvedDependency.getName()))
                        .map(importPath -> importPathToDependency(resolvedDependency, importPath))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toSet());

        Set<GolangDependency> dependencies =
                rootPackagePaths.stream()
                        .<GolangDependency>map(notationParser::parse)
                        .collect(Collectors.toSet());

        return new GolangDependencySet(dependencies);

    }

    private Optional<String> importPathToDependency(ResolvedDependency resolvedDependency, String importPath) {
        if (isBlank(importPath)) {
            return Optional.empty();
        }
        if (isRelativePath(importPath)) {
            return Optional.empty();
        }
        if (isSelfDependency(resolvedDependency, importPath)) {
            return Optional.empty();
        }

        GolangPackage info = packagePathResolver.produce(importPath).get();
        if (info instanceof StandardGolangPackage) {
            return Optional.empty();
        }

        if (info instanceof UnrecognizedGolangPackage) {
            throw DependencyProductionException.cannotRecognizePackage(importPath);
        }

        return Optional.of(info.getRootPath());
    }

    private boolean isSelfDependency(ResolvedDependency resolvedDependency, String importPath) {
        return importPath.startsWith(resolvedDependency.getName());
    }

    private boolean isRelativePath(String importPath) {
        return importPath.startsWith(".");
    }


}
