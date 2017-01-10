package com.github.blindpirate.gogradle.core.dependency.produce;

import com.github.blindpirate.gogradle.core.GolangPackage;
import com.github.blindpirate.gogradle.core.StandardGolangPackage;
import com.github.blindpirate.gogradle.core.dependency.GolangDependency;
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet;
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency;
import com.github.blindpirate.gogradle.core.dependency.parse.NotationParser;
import com.github.blindpirate.gogradle.core.pack.PackagePathResolver;
import com.github.blindpirate.gogradle.util.IOUtils;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.github.blindpirate.gogradle.core.dependency.produce.VendorDependencyFactory.VENDOR_DIRECTORY;
import static com.github.blindpirate.gogradle.util.StringUtils.isBlank;

/**
 * Scans all .go code to generate dependencies.
 */
@Singleton
public class SourceCodeDependencyFactory {

    private static final String TESTDATA_DIRECTORY = "testdata";
    private static final Logger LOGGER = Logging.getLogger(SourceCodeDependencyFactory.class);

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

    public GolangDependencySet produce(ResolvedDependency resolvedDependency, File rootDir) {
        SourceCodeDirectoryVisitor visitor = new SourceCodeDirectoryVisitor();
        IOUtils.walkFileTreeSafely(rootDir.toPath(), visitor);
        return createDependencies(resolvedDependency, visitor.getImportPaths());
    }

    private GolangDependencySet createDependencies(ResolvedDependency resolvedDependency, Set<String> importPaths) {
        Set<String> rootPackagePaths =
                importPaths.stream()
                        .filter(path -> !path.startsWith(resolvedDependency.getName()))
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

        GolangPackage info = packagePathResolver.produce(importPath).get();
        if (info instanceof StandardGolangPackage) {
            return Optional.empty();
        }

        return Optional.of(info.getRootPath());
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
            if (isVendorDirectory(dir) || isTestdataDirectory(dir)) {
                LOGGER.debug("Ignored directory {}", dir);
                return FileVisitResult.SKIP_SUBTREE;
            } else {
                return FileVisitResult.CONTINUE;
            }
        }

        private boolean isTestdataDirectory(Path dir) {
            return directoryNameEquals(dir, TESTDATA_DIRECTORY);
        }

        private boolean directoryNameEquals(Path dir, String name) {
            return name.equals(String.valueOf(dir.getFileName()));
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                throws IOException {
            super.visitFile(file, attrs);

            if (fileShouldBeIncluded(file)) {
                String fileContent = IOUtils.toString(file.toFile());
                importPaths.addAll(goImportExtractor.extract(fileContent));
            } else {
                LOGGER.debug("Ignored file {}" + file);
            }

            return FileVisitResult.CONTINUE;
        }

        private boolean fileShouldBeIncluded(Path file) {
            String fileName = String.valueOf(file.getFileName());
            if (!fileName.endsWith(".go")) {
                return false;
            }
            if (fileName.startsWith(".") || fileName.startsWith("_")) {
                return false;
            }
            return !fileName.endsWith("_test.go");
        }


        private boolean isVendorDirectory(Path dir) {
            return directoryNameEquals(dir, VENDOR_DIRECTORY);
        }

    }

}
