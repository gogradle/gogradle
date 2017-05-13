package com.github.blindpirate.gogradle.common;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

public class MarkDirectoryVisitor extends SimpleFileVisitor<Path> {
    private Set<File> markedDirectories = new HashSet<>();
    private Set<File> ancestorsOfMarkedDirectories = new HashSet<>();
    private Predicate<File> dirPredicate;
    private File rootDir;

    public MarkDirectoryVisitor(Predicate<File> dirPredicate, File rootDir) {
        this.dirPredicate = dirPredicate;
        this.rootDir = rootDir;
    }

    Set<File> getMarkedDirectories() {
        return markedDirectories;
    }

    Set<File> getAncestorsOfMarkedDirectories() {
        return ancestorsOfMarkedDirectories;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dirPath, BasicFileAttributes attrs)
            throws IOException {
        File dir = dirPath.toFile();
        if (dirPredicate.test(dir)) {
            markedDirectories.add(dir);
            while (!rootDir.equals(dir)) {
                dir = dir.getParentFile();
                ancestorsOfMarkedDirectories.add(dir);
            }
            return FileVisitResult.SKIP_SUBTREE;
        } else {
            return FileVisitResult.CONTINUE;
        }
    }
}
