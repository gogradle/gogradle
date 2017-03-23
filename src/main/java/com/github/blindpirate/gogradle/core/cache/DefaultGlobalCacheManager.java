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
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Path;
import java.util.concurrent.Callable;

import static com.github.blindpirate.gogradle.GogradleGlobal.DEFAULT_CHARSET;
import static com.github.blindpirate.gogradle.util.IOUtils.ensureDirExistAndWritable;
import static com.github.blindpirate.gogradle.util.IOUtils.toByteArray;
import static com.github.blindpirate.gogradle.util.IOUtils.write;

@Singleton
public class DefaultGlobalCacheManager implements GlobalCacheManager {
    public static final String GOPATH_CACHE_PATH = "go/gopath";
    public static final String GO_BINARAY_CACHE_PATH = "go/binary";
    public static final String GO_METADATA_PATH = "go/metadata";

    private final GolangPluginSetting setting;

    private final ThreadLocal<FileChannel> fileChannels = new ThreadLocal<>();

    @Inject
    public DefaultGlobalCacheManager(GolangPluginSetting setting) {
        this.setting = setting;
    }

    private Path gradleHome = GradleUserHomeLookup.gradleUserHome().toPath();

    @Override
    public void ensureGlobalCacheExistAndWritable() {
        ensureDirExistAndWritable(gradleHome, GOPATH_CACHE_PATH);
        ensureDirExistAndWritable(gradleHome, GO_BINARAY_CACHE_PATH);
        ensureDirExistAndWritable(gradleHome, GO_METADATA_PATH);
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
            fileChannels.set(channel);
            return callable.call();
        } finally {
            if (lock != null) {
                lock.release();
            }
            if (channel != null) {
                channel.close();
            }
            fileChannels.set(null);
        }
    }

    @Override
    public boolean currentDependencyIsOutOfDate() {
        try {
            // On windows we have to read file like this
            FileChannel currentLockFile = fileChannels.get();
            ByteBuffer buf = ByteBuffer.allocate((int) currentLockFile.size());
            currentLockFile.position(0);
            currentLockFile.read(buf);

            long lastModifiedTime = Long.parseLong(new String(toByteArray(buf), DEFAULT_CHARSET));
            long cacheSecond = setting.getGlobalCacheSecond();
            return System.currentTimeMillis() - lastModifiedTime > DateUtils.toMilliseconds(cacheSecond);
        } catch (IOException e) {
            throw ExceptionHandler.uncheckException(e);
        }
    }

    @Override
    public void updateCurrentDependencyLock() {
        try {
            // On windows we have to write file like this
            FileChannel currentLockFile = fileChannels.get();
            currentLockFile.position(0);
            byte[] bytesToWrite = Long.valueOf(System.currentTimeMillis()).toString().getBytes(DEFAULT_CHARSET);
            currentLockFile.write(ByteBuffer.wrap(bytesToWrite));
        } catch (IOException e) {
            throw ExceptionHandler.uncheckException(e);
        }
    }

    private File createLockFileIfNecessary(GolangDependency dependency) {
        String lockFileName = IOUtils.encodeInternally(dependency.getName());
        File lockFile = gradleHome
                .resolve(GO_METADATA_PATH)
                .resolve(lockFileName)
                .toFile();
        if (!lockFile.exists()) {
            write(lockFile, "0");
        }
        return lockFile;
    }

    private void createPackageDirectoryIfNeccessary(GolangDependency dependency) {
        Path path = getGlobalPackageCachePath(dependency.getName());
        ensureDirExistAndWritable(path);
    }
}
