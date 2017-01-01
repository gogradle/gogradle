package com.github.blindpirate.gogradle.util;

import com.github.blindpirate.gogradle.GolangPluginSetting;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.github.blindpirate.gogradle.GolangPluginSetting.DEFAULT_CHARSET;

/**
 * Encapsulation of {@link FileUtils} and {@link org.apache.commons.io.IOUtils},
 * it never throws checked exceptions.
 */
public class IOUtils {
    public static void forceMkdir(final File directory) {
        try {
            FileUtils.forceMkdir(directory);
        } catch (IOException e) {
            handleIOException(e);
        }
    }

    private static void handleIOException(IOException e) {
        throw new IllegalStateException(e);
    }

    public static void forceDelete(final File file) {
        try {
            FileUtils.forceDelete(file);
        } catch (IOException e) {
            handleIOException(e);
        }
    }

    public static boolean dirIsEmpty(Path dir) {
        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(dir)) {
            return !dirStream.iterator().hasNext();
        } catch (IOException e) {
            handleIOException(e);
            return false;
        }
    }

    public static void ensureDirExistAndWritable(Path path) {
        Assert.isTrue(path.isAbsolute(), "path must be absolute!");
        File dir = path.toFile();
        IOUtils.forceMkdir(dir);
        Assert.isTrue(Files.isWritable(dir.toPath()), "Cannot write to directory:" + path);
    }

    public static void ensureDirExistAndWritable(Path base, String relativePath) {
        ensureDirExistAndWritable(base.resolve(Paths.get(relativePath)));
    }

    public static void copyDirectory(File src, File dest) {
        try {
            org.apache.commons.io.FileUtils.copyDirectory(src, dest);
        } catch (IOException e) {
            handleIOException(e);
        }
    }

    public static void touch(File file) {
        try {
            org.apache.commons.io.FileUtils.touch(file);
        } catch (IOException e) {
            handleIOException(e);
        }
    }

    public static void write(File file, CharSequence data) {
        try {
            org.apache.commons.io.FileUtils.write(file, data, GolangPluginSetting.DEFAULT_CHARSET);
        } catch (IOException e) {
            handleIOException(e);
        }
    }

    public static void write(File baseDir, String fileName, CharSequence data) {
        Path basePath = baseDir.toPath();
        if (fileName.contains("/")) {
            basePath = realBaseDir(basePath, fileName);
            fileName = realFileName(fileName);
            forceMkdir(basePath.toFile());
        }
        write(basePath.resolve(fileName).toFile(), data);
    }

    private static String realFileName(String fileNameWithSlash) {
        Path path = Paths.get(fileNameWithSlash);
        return path.getFileName().toString();
    }

    private static Path realBaseDir(Path basePath, String fileNameWithSlash) {
        Path path = Paths.get(fileNameWithSlash);
        return basePath.resolve(path.subpath(0, path.getNameCount() - 1));
    }

    public static String toString(File file) {
        try {
            return org.apache.commons.io.IOUtils.toString(new FileInputStream(file), DEFAULT_CHARSET);
        } catch (IOException e) {
            handleIOException(e);
            return null;
        }
    }

    public static String toString(InputStream inputStream) {
        try {
            return org.apache.commons.io.IOUtils.toString(inputStream, DEFAULT_CHARSET);
        } catch (IOException e) {
            handleIOException(e);
            return null;
        }
    }

    public static List<String> getLines(File file) {
        String content = toString(file);
        if (StringUtils.isEmpty(content)) {
            return Collections.emptyList();
        } else {
            return Arrays.asList(content.split("\n"));
        }
    }
}
