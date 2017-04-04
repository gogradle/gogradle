package com.github.blindpirate.gogradle.core.cache;

import com.github.blindpirate.gogradle.GolangPluginSetting;
import com.github.blindpirate.gogradle.core.VcsGolangPackage;
import com.github.blindpirate.gogradle.core.dependency.GolangDependency;
import com.github.blindpirate.gogradle.core.dependency.NotationDependency;
import com.github.blindpirate.gogradle.util.ExceptionHandler;
import com.github.blindpirate.gogradle.util.IOUtils;
import com.github.blindpirate.gogradle.util.StringUtils;
import com.github.blindpirate.gogradle.vcs.GitMercurialNotationDependency;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.wrapper.GradleUserHomeLookup;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

import static com.github.blindpirate.gogradle.GogradleGlobal.DEFAULT_CHARSET;
import static com.github.blindpirate.gogradle.util.DataExchange.parseYaml;
import static com.github.blindpirate.gogradle.util.DataExchange.toYaml;
import static com.github.blindpirate.gogradle.util.DateUtils.toMilliseconds;
import static com.github.blindpirate.gogradle.util.IOUtils.ensureDirExistAndWritable;
import static com.github.blindpirate.gogradle.util.IOUtils.toByteArray;

@Singleton
public class DefaultGlobalCacheManager implements GlobalCacheManager {
    public static final String GOPATH_CACHE_PATH = "go/gopath";
    public static final String GO_BINARAY_CACHE_PATH = "go/binary";
    public static final String GO_METADATA_PATH = "go/metadata";
    private static final Logger LOGGER = Logging.getLogger(DefaultGlobalCacheManager.class);
    private static final int DEFAULT_CREATE_LOCKFILE_RETRY_COUNT = 10;

    private boolean initialized = false;

    private final GolangPluginSetting setting;

    private final ThreadLocal<FileChannel> fileChannels = new ThreadLocal<>();

    @Inject
    public DefaultGlobalCacheManager(GolangPluginSetting setting) {
        this.setting = setting;
    }

    private Path gradleHome = GradleUserHomeLookup.gradleUserHome().toPath();

    @Override
    public void ensureGlobalCacheExistAndWritable() {
        if (initialized) {
            return;
        }
        ensureDirExistAndWritable(gradleHome, GOPATH_CACHE_PATH);
        ensureDirExistAndWritable(gradleHome, GO_BINARAY_CACHE_PATH);
        ensureDirExistAndWritable(gradleHome, GO_METADATA_PATH);
        initialized = true;
    }

    @Override
    public Path getGlobalPackageCachePath(String packagePath) {
        ensureGlobalCacheExistAndWritable();
        return gradleHome.resolve(GOPATH_CACHE_PATH).resolve(packagePath);
    }

    @Override
    public Path getGlobalGoBinCache(String relativePath) {
        ensureGlobalCacheExistAndWritable();
        return gradleHome.resolve(GO_BINARAY_CACHE_PATH).resolve(relativePath);
    }

    private Path getGlobalMetadata(String packagePath) {
        ensureGlobalCacheExistAndWritable();
        return gradleHome.resolve(GO_METADATA_PATH).resolve(IOUtils.encodeInternally(packagePath));
    }

    @Override
    public Optional<GlobalCacheMetadata> getMetadata(Path packagePath) {
        ensureGlobalCacheExistAndWritable();
        try {
            return doGetMetadata(packagePath);
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    private Optional<GlobalCacheMetadata> doGetMetadata(Path packagePath) throws IOException {
        Path lockfilePath = getGlobalMetadata(StringUtils.toUnixString(packagePath));
        FileChannel channel = null;
        FileLock lock = null;
        try {
            channel = new RandomAccessFile(lockfilePath.toFile(), "r").getChannel();
            // Here we must use tryLock to avoid dead-lock
            lock = channel.tryLock(0L, Long.MAX_VALUE, true);
            if (lock == null) {
                return Optional.empty();
            } else {
                return getMetadataFromFile(channel);
            }
        } catch (FileNotFoundException e) {
            return Optional.empty();
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
    public synchronized <T> T runWithGlobalCacheLock(GolangDependency dependency, Callable<T> callable)
            throws Exception {
        ensureGlobalCacheExistAndWritable();
        FileChannel channel = null;
        FileLock lock = null;
        createPackageDirectoryIfNecessary(dependency);
        try {
            channel = createLockFileIfNecessary(dependency);
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
        ensureGlobalCacheExistAndWritable();
        try {
            // On windows we have to read file like this
            Optional<GlobalCacheMetadata> metadata = getMetadataFromFile(fileChannels.get());
            return determineIfOutOfDate(dependency, metadata);
        } catch (IOException e) {
            throw ExceptionHandler.uncheckException(e);
        }
    }

    private Optional<GlobalCacheMetadata> getMetadataFromFile(FileChannel fileChannel) throws IOException {
        ByteBuffer buf = ByteBuffer.allocate((int) fileChannel.size());
        fileChannel.position(0);
        fileChannel.read(buf);

        String fileContent = new String(toByteArray(buf), DEFAULT_CHARSET);
        if (StringUtils.isEmpty(fileContent)) {
            return Optional.empty();
        } else {
            return Optional.of(parseYaml(fileContent, GlobalCacheMetadata.class));
        }
    }

    private boolean determineIfOutOfDate(NotationDependency dependency, Optional<GlobalCacheMetadata> metadata) {
        if (!metadata.isPresent()) {
            return true;
        }
        List<String> urls = GitMercurialNotationDependency.class.cast(dependency).getUrls();
        if (urls.contains(metadata.get().getLastUpdateUrl())) {
            long cacheSecond = setting.getGlobalCacheSecond();
            return System.currentTimeMillis() - metadata.get().getLastUpdateTime() > toMilliseconds(cacheSecond);
        } else {
            return true;
        }
    }

    @Override
    public void updateCurrentDependencyLock(GolangDependency dependency) {
        ensureGlobalCacheExistAndWritable();
        try {
            // On windows we have to write file like this
            FileChannel currentLockFile = fileChannels.get();
            byte[] bytesToWrite = toYaml(createOrUpdateMetadata(dependency)).getBytes(DEFAULT_CHARSET);
            currentLockFile.position(0);
            currentLockFile.truncate(bytesToWrite.length);
            currentLockFile.write(ByteBuffer.wrap(bytesToWrite));
        } catch (IOException e) {
            throw ExceptionHandler.uncheckException(e);
        }
    }

    private FileChannel createLockFileIfNecessary(GolangDependency dependency) {
        File lockFile = getGlobalMetadata(dependency.getName()).toFile();
        int retryCount = DEFAULT_CREATE_LOCKFILE_RETRY_COUNT;
        while (retryCount-- > 0) {
            try {
                return new RandomAccessFile(lockFile, "rwd").getChannel();
            } catch (FileNotFoundException e) {
                LOGGER.debug("try to create file {} failed, retry count {}",
                        lockFile.getAbsolutePath(),
                        retryCount);
            }
        }
        throw new IllegalStateException("Fail to create lock file " + lockFile.getAbsolutePath());
    }

    private GlobalCacheMetadata newMetadata(GolangDependency dependency) {
        VcsGolangPackage pkg = (VcsGolangPackage) dependency.getPackage();
        return GlobalCacheMetadata.newMetadata(pkg);
    }

    private GlobalCacheMetadata createOrUpdateMetadata(GolangDependency dependency) throws IOException {
        Optional<GlobalCacheMetadata> optionalOldMetadata = getMetadataFromFile(fileChannels.get());

        if (optionalOldMetadata.isPresent()) {
            GlobalCacheMetadata oldMetadata = optionalOldMetadata.get();
            VcsGolangPackage pkg = (VcsGolangPackage) dependency.getPackage();
            Path cacheRoot = getGlobalPackageCachePath(pkg.getRootPathString());

            String url = pkg.getVcsType().getAccessor().getRemoteUrl(cacheRoot.toFile());
            oldMetadata.update(url);
            return oldMetadata;
        } else {
            return newMetadata(dependency);
        }
    }

    private void createPackageDirectoryIfNecessary(GolangDependency dependency) {
        Path path = getGlobalPackageCachePath(dependency.getName());
        ensureDirExistAndWritable(path);
    }
}
