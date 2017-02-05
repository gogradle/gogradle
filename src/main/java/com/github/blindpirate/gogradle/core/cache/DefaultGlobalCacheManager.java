package com.github.blindpirate.gogradle.core.cache;

import com.github.blindpirate.gogradle.GolangPluginSetting;
import com.github.blindpirate.gogradle.core.dependency.GolangDependency;
import com.github.blindpirate.gogradle.util.DateUtils;
import com.github.blindpirate.gogradle.util.ExceptionHandler;
import com.github.blindpirate.gogradle.util.IOUtils;
import org.gradle.wrapper.GradleUserHomeLookup;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Path;
import java.util.concurrent.Callable;

import static com.github.blindpirate.gogradle.GogradleGlobal.DEFAULT_CHARSET;
import static com.github.blindpirate.gogradle.util.IOUtils.ensureDirExistAndWritable;

@Singleton
public class DefaultGlobalCacheManager implements GlobalCacheManager {
    public static final String GOPATH_CACHE_PATH = "go/gopath";
    public static final String GO_BINARAY_CACHE_PATH = "go/binary";
    public static final String GO_LOCKFILES_PATH = "go/lockfiles";

    private final GolangPluginSetting setting;

    @Inject
    public DefaultGlobalCacheManager(GolangPluginSetting setting) {
        this.setting = setting;
    }

    private Path gradleHome = GradleUserHomeLookup.gradleUserHome().toPath();

    @Override
    public void ensureGlobalCacheExistAndWritable() {
        ensureDirExistAndWritable(gradleHome, GOPATH_CACHE_PATH);
        ensureDirExistAndWritable(gradleHome, GO_BINARAY_CACHE_PATH);
        ensureDirExistAndWritable(gradleHome, GO_LOCKFILES_PATH);
    }

    @Override
    public Path getGlobalPackageCachePath(String packagePath) {
        return gradleHome.resolve(GOPATH_CACHE_PATH).resolve(packagePath);
    }

    @Override
    public Path getGlobalGoBinCache(String relativePath) {
        return gradleHome.resolve(GO_BINARAY_CACHE_PATH).resolve(relativePath);
    }

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

    @Override
    public boolean isOutOfDate(GolangDependency dependency) {
        long lastModifiedTime = Long.parseLong(IOUtils.toString(createLockFileIfNecessary(dependency)));
        long cacheSecond = setting.getGlobalCacheSecond();
        return System.currentTimeMillis() - lastModifiedTime > DateUtils.toMilliseconds(cacheSecond);
    }

    @Override
    public void updateLockFile(GolangDependency dependency) {
        File lockFile = createLockFileIfNecessary(dependency);
        IOUtils.write(lockFile, "" + System.currentTimeMillis());
    }

    private File createLockFileIfNecessary(GolangDependency dependency) {
        try {
            String lockFileName = URLEncoder.encode(dependency.getName(), DEFAULT_CHARSET);
            File lockFile = gradleHome
                    .resolve(GO_LOCKFILES_PATH)
                    .resolve(lockFileName)
                    .toFile();
            if (!lockFile.exists()) {
                IOUtils.write(lockFile, "0");
            }
            return lockFile;
        } catch (UnsupportedEncodingException e) {
            throw ExceptionHandler.uncheckException(e);
        }
    }

    private void createPackageDirectoryIfNeccessary(GolangDependency dependency) {
        Path path = getGlobalPackageCachePath(dependency.getName());
        IOUtils.ensureDirExistAndWritable(path);
    }
}
