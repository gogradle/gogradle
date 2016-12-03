package com.github.blindpirate.gogradle.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

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
}
