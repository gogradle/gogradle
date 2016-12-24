package com.github.blindpirate.gogradle.core.dependency.resolve;

import com.github.blindpirate.gogradle.core.GolangPackageModule;
import com.github.blindpirate.gogradle.core.dependency.GolangDependency;
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet;
import com.github.blindpirate.gogradle.core.dependency.parse.NotationParser;
import com.github.blindpirate.gogradle.core.dependency.produce.GoImportExtractor;
import com.github.blindpirate.gogradle.core.exceptions.DependencyResolutionException;
import com.github.blindpirate.gogradle.core.pack.PackageInfo;
import com.github.blindpirate.gogradle.core.pack.PackageNameResolver;
import com.github.blindpirate.gogradle.util.IOUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.github.blindpirate.gogradle.GolangPluginSetting.MAX_DIRECTORY_WALK_DEPTH;
import static com.github.blindpirate.gogradle.core.dependency.resolve.VendorDependencyFactory.VENDOR_DIRECTORY;
import static com.github.blindpirate.gogradle.util.StringUtils.isBlank;

/**
 * Scans all .go code to generate dependencies.
 */
@Singleton
public class SourceCodeDependencyFactory implements DependencyFactory {
    private final GoImportExtractor goImportExtractor = new GoImportExtractor();
    private final PackageNameResolver packageNameResolver;
    private final NotationParser notationParser;

    @Inject
    public SourceCodeDependencyFactory(PackageNameResolver packageNameResolver,
                                       NotationParser notationParser) {
        this.packageNameResolver = packageNameResolver;
        this.notationParser = notationParser;
    }

    @Override
    public Optional<GolangDependencySet> produce(GolangPackageModule module) {
        SourceCodeDirectoryVisitor visitor = new SourceCodeDirectoryVisitor();
        try {
            Files.walkFileTree(module.getRootDir(),
                    Collections.<FileVisitOption>emptySet(),
                    MAX_DIRECTORY_WALK_DEPTH,
                    visitor);
        } catch (IOException e) {
            throw DependencyResolutionException.sourceCodeParsingFailed(module, e);
        }
        return Optional.of(createDependencies(visitor.getImportPaths()));
    }

    private GolangDependencySet createDependencies(Set<String> importPaths) {
        Set<String> rootPackagePaths =
                importPaths.stream()
                        .map(this::importPathToDependency)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toSet());

        Set<GolangDependency> dependencies =
                rootPackagePaths.stream()
                        .<GolangDependency>map(notationParser::parse)
                        .collect(Collectors.toSet());

        return new GolangDependencySet(dependencies);

    }

    private Optional<String> importPathToDependency(String importPath) {
        if (isBlank(importPath)) {
            return Optional.empty();
        }
        if (isRelativePath(importPath)) {
            return Optional.empty();
        }

        PackageInfo info = packageNameResolver.produce(importPath).get();
        if (info.isStandard()) {
            return Optional.empty();
        }

        return Optional.of(info.getRootName());
    }

    private boolean isRelativePath(String importPath) {
        return importPath.startsWith(".");
    }

    private class SourceCodeDirectoryVisitor extends SimpleFileVisitor<Path> {
        private Set<String> importPaths = new HashSet<>();

        public Set<String> getImportPaths() {
            return importPaths;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                throws IOException {
            super.preVisitDirectory(dir, attrs);
            if (isVendorDirectory(dir)) {
                return FileVisitResult.SKIP_SUBTREE;
            } else {
                return FileVisitResult.CONTINUE;
            }
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                throws IOException {
            super.visitFile(file, attrs);

            if (String.valueOf(file.getFileName()).endsWith(".go")) {
                String fileContent = IOUtils.toString(file.toFile());
                importPaths.addAll(goImportExtractor.extract(fileContent));
            }

            return FileVisitResult.CONTINUE;
        }


        private boolean isVendorDirectory(Path dir) {
            return VENDOR_DIRECTORY.equals(String.valueOf(dir.getFileName()));
        }

    }

}
