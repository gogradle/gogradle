package com.github.blindpirate.gogradle.util;

import com.github.blindpirate.gogradle.GolangPluginSetting;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.github.blindpirate.gogradle.GolangPluginSetting.DEFAULT_CHARSET;

public class IOUtils {
    public static void forceMkdir(final File directory) throws IOException {
        org.apache.commons.io.FileUtils.forceMkdir(directory);
    }

    public static void forceDelete(final File file) throws IOException {
        org.apache.commons.io.FileUtils.forceDelete(file);
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
            IOUtils.forceMkdir(dir);
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

    public static void touch(File file) {
        try {
            org.apache.commons.io.FileUtils.touch(file);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static void write(File file, CharSequence data) {
        try {
            org.apache.commons.io.FileUtils.write(file, data, GolangPluginSetting.DEFAULT_CHARSET);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static void write(File dir, String fileName, CharSequence data) {
        write(dir.toPath().resolve(fileName).toFile(), data);
    }

    public static String toString(File file) {
        try {
            return org.apache.commons.io.IOUtils.toString(new FileInputStream(file), DEFAULT_CHARSET);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static String toString(InputStream inputStream) {
        try {
            return org.apache.commons.io.IOUtils.toString(inputStream, DEFAULT_CHARSET);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
