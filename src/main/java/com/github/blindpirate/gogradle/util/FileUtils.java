package com.github.blindpirate.gogradle.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtils {
    public static void forceMkdir(final File directory) throws IOException {
        org.apache.commons.io.FileUtils.forceMkdir(directory);
    }

    public static void forceDelete(final File file) throws IOException {
        org.apache.commons.io.FileUtils.forceDelete(file);
    }


    public static File locate(File dir, String relativePath) {
        return new File(dir, relativePath);
    }

    public static File locate(Path path, String relativePath) {
        return path.resolve(relativePath).toFile();
    }

    public static boolean dirIsEmpty(Path dir) {
        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(dir)) {
            return !dirStream.iterator().hasNext();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static void ensureExistAndWritable(Path path) {
        Assert.isTrue(path.isAbsolute(), "path must be absolute!");
        File dir = path.toFile();
        try {
            FileUtils.forceMkdir(dir);
        } catch (IOException e) {
            throw new RuntimeException("Create directory "
                    + path
                    + " failed, please check if you have access to it.");
        }
        Assert.isTrue(Files.isWritable(dir.toPath()), "Cannot write to directory:" + path);
    }

    public static void ensureExsitAndWritable(Path base, String relativePath) {
        ensureExistAndWritable(base.resolve(Paths.get(relativePath)));
    }

    public static void copyDirectory(File src, File dest) {
        try {
            org.apache.commons.io.FileUtils.copyDirectory(src, dest);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
