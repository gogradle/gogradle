package com.github.blindpirate.gogradle.core.cache;

import com.github.blindpirate.gogradle.core.dependency.GolangDependency;
import com.github.blindpirate.gogradle.util.FileUtils;
import org.gradle.wrapper.GradleUserHomeLookup;

import javax.inject.Singleton;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Path;
import java.util.concurrent.Callable;

import static com.github.blindpirate.gogradle.util.FileUtils.ensureExsitAndWritable;

@Singleton
public class DefaultCacheManager implements CacheManager {
    private static final String GOPATH_CACHE_PATH = "go/gopath";
    private static final String GO_BINARAY_CACHE_PATH = "go/binary";
    private static final String GO_LOCKFILES_PATH = "go/lockfiles";
    private Path gradleHome = GradleUserHomeLookup.gradleUserHome().toPath();

    @Override
    public void ensureGlobalCacheExistAndWritable() {
        ensureExsitAndWritable(gradleHome, GOPATH_CACHE_PATH);
        ensureExsitAndWritable(gradleHome, GO_BINARAY_CACHE_PATH);
    }

    @Override
    public void ensureProjectBuildCacheExist() {
    }

    @Override
    public Path getGlobalCachePath(String packageName) {
        return gradleHome.resolve(GOPATH_CACHE_PATH).resolve(packageName);
    }

    // TODO can package a/b/c and package a/b be concurrent?
    @Override
    public synchronized <T> T runWithGlobalCacheLock(GolangDependency dependency, Callable<T> callable) {
        FileChannel channel = null;
        FileLock lock = null;
        createPackageDirectoryIfNeccessary(dependency);
        File lockFile = createLockFileIfNecessary(dependency);
        try {
            channel = new RandomAccessFile(lockFile, "rw").getChannel();
            lock = channel.lock();
            return callable.call();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            // if getting lock failed, we should be here
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (lock != null) {
                try {
                    lock.release();
                } catch (IOException e) {

                }
            }
            if (channel != null) {
                try {
                    channel.close();
                } catch (IOException e) {

                }
            }
        }
        return null;
    }

    private File createLockFileIfNecessary(GolangDependency dependency) {
        String lockFileName = dependency.getName().replaceAll("/", "_");
        File lockFile = gradleHome
                .resolve(GO_LOCKFILES_PATH)
                .resolve(lockFileName)
                .toFile();
        FileUtils.touch(lockFile);
        return lockFile;
    }

    private void createPackageDirectoryIfNeccessary(GolangDependency dependency) {
        Path path = getGlobalCachePath(dependency.getName());
        FileUtils.ensureExistAndWritable(path);
    }
}
