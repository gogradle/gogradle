package com.github.blindpirate.gogradle.util;

import com.github.blindpirate.gogradle.build.TestPatternFilter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import static com.github.blindpirate.gogradle.GogradleGlobal.DEFAULT_CHARSET;
import static com.github.blindpirate.gogradle.GogradleGlobal.MAX_DFS_DEPTH;

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

    public static File mkdir(File baseDir, String newDirName) {
        File ret = new File(baseDir, newDirName);
        forceMkdir(ret);
        return ret;
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

    public static boolean dirIsEmpty(File directory) {
        String[] files = Assert.isNotNull(directory.list());
        return files.length == 0;
    }

    public static Path ensureDirExistAndWritable(Path path) {
        Assert.isTrue(path.isAbsolute(), "path must be absolute!");
        File dir = path.toFile();
        IOUtils.forceMkdir(dir);
        Assert.isTrue(Files.isWritable(dir.toPath()), "Cannot write to directory:" + path);
        return path;
    }

    public static Path ensureDirExistAndWritable(Path base, String relativePath) {
        return ensureDirExistAndWritable(base.resolve(Paths.get(relativePath)));
    }

    public static void copyDirectory(File src, File dest) {
        try {
            org.apache.commons.io.FileUtils.copyDirectory(src, dest);
        } catch (IOException e) {
            handleIOException(e);
        }
    }

    public static void copyDirectory(final File srcDir, final File destDir,
                                     final FileFilter filter) {
        try {
            FileUtils.copyDirectory(srcDir, destDir, filter);
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
            org.apache.commons.io.FileUtils.write(file, data, DEFAULT_CHARSET);
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
        return String.valueOf(path.getFileName());
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

    public static List<String> safeList(File dir) {
        String[] files = dir.list();
        return files == null ? Collections.emptyList() : Arrays.asList(files);
    }

    public static boolean isValidDirectory(File dir) {
        return dir.isDirectory() && dir.exists();
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

    public static void walkFileTreeSafely(Path path, FileVisitor<? super Path> visitor) {
        try {
            Files.walkFileTree(path, EnumSet.noneOf(FileVisitOption.class), MAX_DFS_DEPTH, visitor);
        } catch (IOException e) {
            handleIOException(e);
        }
    }

    public static Collection<File> filterTestsMatchingPatterns(File dir, List<String> namePattern) {
        TestPatternFilter filter = new TestPatternFilter(namePattern);
        return FileUtils.listFiles(dir, filter, filter);
    }

    public static void clearDirectory(File dir) {
        try {
            FileUtils.cleanDirectory(dir);
        } catch (IOException e) {
            handleIOException(e);
        }
    }

    public static void chmodAddX(Path filePath) {
        try {
            Files.setPosixFilePermissions(filePath, PosixFilePermissions.fromString("rwx------"));
        } catch (IOException e) {
            handleIOException(e);
        }

    }
}
