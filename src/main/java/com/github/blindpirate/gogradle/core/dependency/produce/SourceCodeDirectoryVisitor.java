package com.github.blindpirate.gogradle.core.dependency.produce;

import com.github.blindpirate.gogradle.build.Configuration;
import com.github.blindpirate.gogradle.util.IOUtils;
import com.google.common.collect.ImmutableMap;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import static com.github.blindpirate.gogradle.core.dependency.produce.SourceCodeDependencyFactory.TESTDATA_DIRECTORY;
import static com.github.blindpirate.gogradle.core.dependency.produce.VendorDependencyFactory.VENDOR_DIRECTORY;
import static com.github.blindpirate.gogradle.util.StringUtils.fileNameEqualsAny;
import static com.github.blindpirate.gogradle.util.StringUtils.fileNameStartsWithAny;

class SourceCodeDirectoryVisitor extends SimpleFileVisitor<Path> {
    private static final Logger LOGGER = Logging.getLogger(SourceCodeDirectoryVisitor.class);

    private static Map<Configuration, Predicate<String>> predicates = ImmutableMap.of(
            Configuration.BUILD, SourceCodeDirectoryVisitor::isBuildGoFile,
            Configuration.TEST, SourceCodeDirectoryVisitor::isTestGoFile
    );

    private Set<String> importPaths = new HashSet<>();

    private GoImportExtractor goImportExtractor;

    private Predicate<String> predicate;

    public SourceCodeDirectoryVisitor(Configuration configuration, GoImportExtractor goImportExtractor) {
        this.predicate = predicates.get(configuration);
        this.goImportExtractor = goImportExtractor;
    }

    public Set<String> getImportPaths() {
        return importPaths;
    }

    private static boolean isBuildGoFile(String fileName) {
        return fileName.endsWith(".go") && !fileName.endsWith("_test.go");
    }

    private static boolean isTestGoFile(String fileName) {
        return fileName.endsWith("_test.go");
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
            throws IOException {
        super.preVisitDirectory(dir, attrs);
        if (dirShouldBeIncluded(dir)) {
            return FileVisitResult.CONTINUE;
        } else {
            LOGGER.debug("Ignored directory {}", dir);
            return FileVisitResult.SKIP_SUBTREE;
        }
    }

    private boolean dirShouldBeIncluded(Path dir) {
        if (fileNameEqualsAny(dir.toFile(), TESTDATA_DIRECTORY, VENDOR_DIRECTORY)) {
            return false;
        }
        if (fileNameStartsWithAny(dir.toFile(), "_", ".")) {
            return false;
        }
        return true;
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
        if (fileNameStartsWithAny(file.toFile(), ".", "_")) {
            return false;
        }
        return predicate.test(fileName);
    }

}
