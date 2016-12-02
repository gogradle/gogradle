package com.github.blindpirate.gogradle.util;

import org.gradle.api.file.ConfigurableFileTree;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class FileUtils {
    public static void forceMkdir(final File directory) throws IOException {
        org.apache.commons.io.FileUtils.forceMkdir(directory);
    }


    public static File locate(File dir, String relativePath) {
        return new File(dir, relativePath);
    }

    public static File locate(Path path, String relativePath) {
        return path.resolve(relativePath).toFile();
    }
}