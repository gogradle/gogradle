package com.github.blindpirate.gogradle.common;

import com.github.blindpirate.gogradle.util.IOUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class DeleteUnmarkedDirectoryVistor extends SimpleFileVisitor<Path> {
    MarkDirectoryVisitor markDirectoryVisitor;

    public DeleteUnmarkedDirectoryVistor(MarkDirectoryVisitor markDirectoryVisitor) {
        this.markDirectoryVisitor = markDirectoryVisitor;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dirPath, BasicFileAttributes attrs)
            throws IOException {
        File dir = dirPath.toFile();
        if (isMarked(dir)) {
            return FileVisitResult.SKIP_SUBTREE;
        } else if (isAncestorOfMarked(dir)) {
            return FileVisitResult.CONTINUE;
        } else {
            IOUtils.forceDelete(dir);
            return FileVisitResult.SKIP_SUBTREE;
        }
    }

    private boolean isMarked(File dir) {
        return markDirectoryVisitor.getMarkedDirectories().contains(dir);
    }

    private boolean isAncestorOfMarked(File dir) {
        return markDirectoryVisitor.getAncestorsOfMarkedDirectories().contains(dir);
    }
}
