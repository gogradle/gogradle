package com.github.blindpirate.gogradle.core.cache;

import com.github.blindpirate.gogradle.GolangPluginSetting;
import com.github.blindpirate.gogradle.core.VcsGolangPackage;
import com.github.blindpirate.gogradle.core.dependency.GolangDependency;
import com.github.blindpirate.gogradle.core.dependency.NotationDependency;
import com.github.blindpirate.gogradle.util.DateUtils;
import com.github.blindpirate.gogradle.util.ExceptionHandler;
import com.github.blindpirate.gogradle.util.IOUtils;
import com.github.blindpirate.gogradle.util.StringUtils;
import com.github.blindpirate.gogradle.vcs.GitMercurialNotationDependency;
import org.gradle.wrapper.GradleUserHomeLookup;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

import static com.github.blindpirate.gogradle.GogradleGlobal.DEFAULT_CHARSET;
import static com.github.blindpirate.gogradle.util.DataExchange.parseYaml;
import static com.github.blindpirate.gogradle.util.DataExchange.toYaml;
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

    private Path getGlobalMetadata(String packagePath) {
        return gradleHome.resolve(GO_METADATA_PATH).resolve(IOUtils.encodeInternally(packagePath));
    }

    @Override
    public Optional<GlobalCacheMetadata> getMetadata(Path packagePath) {
        try {
            return doGetMetadata(packagePath);
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    private Optional<GlobalCacheMetadata> doGetMetadata(Path packagePath) throws IOException {
        Path lockfilePath = getGlobalMetadata(StringUtils.toUnixString(packagePath));
        if (Files.exists(lockfilePath)) {
            FileChannel channel = null;
            FileLock lock = null;
            try {
                channel = new RandomAccessFile(lockfilePath.toFile(), "rw").getChannel();
                // Here we must use tryLock to avoid dead-lock
                lock = channel.tryLock();
                if (lock == null) {
                    return Optional.empty();
                } else {
                    return Optional.of(readFile(channel));
                }
            } finally {
                if (lock != null) {
                    lock.release();
                }
                if (channel != null) {
                    channel.close();
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public synchronized <T> T runWithGlobalCacheLock(GolangDependency dependency, Callable<T> callable)
            throws Exception {
        FileChannel channel = null;
        FileLock lock = null;
        createPackageDirectoryIfNecessary(dependency);
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
    public boolean currentDependencyIsOutOfDate(NotationDependency dependency) {
        try {
            // On windows we have to read file like this
            GlobalCacheMetadata metadata = readFile(fileChannels.get());
            return determineIfOutOfDate(dependency, metadata);
        } catch (IOException e) {
            throw ExceptionHandler.uncheckException(e);
        }
    }

    private GlobalCacheMetadata readFile(FileChannel fileChannel) throws IOException {
        ByteBuffer buf = ByteBuffer.allocate((int) fileChannel.size());
        fileChannel.position(0);
        fileChannel.read(buf);

        String fileContent = new String(toByteArray(buf), DEFAULT_CHARSET);
        return parseYaml(fileContent, GlobalCacheMetadata.class);
    }

    private boolean determineIfOutOfDate(NotationDependency dependency, GlobalCacheMetadata metadata) {
        List<String> urls = GitMercurialNotationDependency.class.cast(dependency).getUrls();
        if (urls.contains(metadata.getLastUpdateUrl())) {
            long cacheSecond = setting.getGlobalCacheSecond();
            return System.currentTimeMillis() - metadata.getLastUpdateTime() > DateUtils.toMilliseconds(cacheSecond);
        } else {
            return true;
        }
    }

    @Override
    public void updateCurrentDependencyLock(NotationDependency dependency) {
        try {
            // On windows we have to write file like this
            FileChannel currentLockFile = fileChannels.get();
            currentLockFile.position(0);
            byte[] bytesToWrite = toYaml(updatedMetaData(dependency)).getBytes(DEFAULT_CHARSET);
            currentLockFile.write(ByteBuffer.wrap(bytesToWrite));
        } catch (IOException e) {
            throw ExceptionHandler.uncheckException(e);
        }
    }

    private File createLockFileIfNecessary(GolangDependency dependency) {
        File lockFile = getGlobalMetadata(dependency.getName()).toFile();
        if (!lockFile.exists()) {
            write(lockFile, toYaml(newMetadata((NotationDependency) dependency)));
        }
        return lockFile;
    }

    private GlobalCacheMetadata newMetadata(NotationDependency dependency) {
        VcsGolangPackage pkg = (VcsGolangPackage) dependency.getPackage();
        return GlobalCacheMetadata.newMetadata(pkg);
    }

    private GlobalCacheMetadata updatedMetaData(NotationDependency dependency) {
        VcsGolangPackage pkg = (VcsGolangPackage) dependency.getPackage();
        Path cacheRoot = getGlobalPackageCachePath(pkg.getRootPathString());

        String url = pkg.getVcsType().getAccessor().getRemoteUrl(cacheRoot.toFile());
        return GlobalCacheMetadata.updatedMetadata(pkg, url);
    }

    private void createPackageDirectoryIfNecessary(GolangDependency dependency) {
        Path path = getGlobalPackageCachePath(dependency.getName());
        ensureDirExistAndWritable(path);
    }
}
