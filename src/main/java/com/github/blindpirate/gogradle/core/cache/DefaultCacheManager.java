package com.github.blindpirate.gogradle.core.cache;

import com.github.blindpirate.gogradle.GolangPluginSetting;
import com.github.blindpirate.gogradle.core.dependency.GolangDependency;
import com.github.blindpirate.gogradle.util.IOUtils;
import org.gradle.wrapper.GradleUserHomeLookup;

import javax.inject.Singleton;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Path;
import java.util.concurrent.Callable;

import static com.github.blindpirate.gogradle.GolangPluginSetting.*;
import static com.github.blindpirate.gogradle.util.IOUtils.ensureExsitAndWritable;

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
    public synchronized <T> T runWithGlobalCacheLock(GolangDependency dependency, Callable<T> callable)
            throws Exception {
        FileChannel channel = null;
        FileLock lock = null;
        createPackageDirectoryIfNeccessary(dependency);
        File lockFile = createLockFileIfNecessary(dependency);
        try {
            channel = new RandomAccessFile(lockFile, "rw").getChannel();
            lock = channel.lock();
            return callable.call();
        } finally {
            if (lock != null) {
                lock.release();
            }
            if (channel != null) {
                channel.close();
            }
        }
    }

    private File createLockFileIfNecessary(GolangDependency dependency) throws UnsupportedEncodingException {
        String lockFileName = URLEncoder.encode(dependency.getName(), DEFAULT_CHARSET);
        File lockFile = gradleHome
                .resolve(GO_LOCKFILES_PATH)
                .resolve(lockFileName)
                .toFile();
        IOUtils.touch(lockFile);
        return lockFile;
    }

    private void createPackageDirectoryIfNeccessary(GolangDependency dependency) {
        Path path = getGlobalCachePath(dependency.getName());
        IOUtils.ensureExistAndWritable(path);
    }
}
