/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.github.blindpirate.gogradle.core.cache;

import com.github.blindpirate.gogradle.GolangPluginSetting;
import com.github.blindpirate.gogradle.core.GolangRepository;
import com.github.blindpirate.gogradle.core.VcsGolangPackage;
import com.github.blindpirate.gogradle.util.Assert;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.wrapper.GradleUserHomeLookup;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

import static com.github.blindpirate.gogradle.GogradleGlobal.DEFAULT_CHARSET;
import static com.github.blindpirate.gogradle.core.cache.GlobalCacheMetadata.GolangRepositoryMetadata;
import static com.github.blindpirate.gogradle.util.DataExchange.parseYaml;
import static com.github.blindpirate.gogradle.util.DataExchange.toYaml;
import static com.github.blindpirate.gogradle.util.DateUtils.toMilliseconds;
import static com.github.blindpirate.gogradle.util.IOUtils.ensureDirExistAndWritable;
import static com.github.blindpirate.gogradle.util.IOUtils.toByteArray;
import static com.github.blindpirate.gogradle.util.StringUtils.isEmpty;
import static com.github.blindpirate.gogradle.util.StringUtils.toUnixString;

@Singleton
public class DefaultGlobalCacheManager implements GlobalCacheManager {
    public static final String REPO_CACHE_PATH = "go/repo";
    public static final String GO_BINARAY_CACHE_PATH = "go/binary";
    private static final String METADATA_FILE_NAME = "gogradle-metadata";
    private static final Logger LOGGER = Logging.getLogger(DefaultGlobalCacheManager.class);
    private static final int DEFAULT_CREATE_LOCKFILE_RETRY_COUNT = 10;

    private boolean initialized = false;

    private final GolangPluginSetting setting;

    private final ThreadLocal<Session> sessions = new ThreadLocal<>();

    private final ReentrantLock lock = new ReentrantLock();

    @SuppressFBWarnings("SIC_THREADLOCAL_DEADLY_EMBRACE")
    private class Session {
        private FileChannel fileChannel;
        private GlobalCacheMetadata metadata;
        private GolangRepositoryMetadata repositoryMetadata;
        private FileLock fileLock;
        private File repoRoot;

        private Session(VcsGolangPackage pkg) throws IOException {
            File lockFile = getMetadataPath(pkg);
            ensureDirExistAndWritable(lockFile.getParentFile().toPath());

            this.fileChannel = createLockFileIfNecessary(lockFile);
            this.fileLock = fileChannel.lock();
            this.metadata = getMetadataFromFile(fileChannel).orElse(GlobalCacheMetadata.newMetadata(pkg));

            Optional<GolangRepositoryMetadata> matched = findMatchedRepository(metadata, pkg.getRepository());
            if (!matched.isPresent()) {
                metadata.addRepository(pkg.getRepository());
                matched = findMatchedRepository(metadata, pkg.getRepository());
            }

            repositoryMetadata = matched.get();
            repoRoot = getRepoPath(pkg, repositoryMetadata.getDir());
        }

        private Session cleanUp() throws IOException {
            updateMetadata();
            fileLock.release();
            fileChannel.close();
            return this;
        }

        private void updateMetadata() throws IOException {
            if (metadata.isDirty()) {
                byte[] bytesToWrite = toYaml(metadata).getBytes(DEFAULT_CHARSET);
                fileChannel.position(0);
                fileChannel.truncate(bytesToWrite.length);
                fileChannel.write(ByteBuffer.wrap(bytesToWrite));
            }
        }
    }


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
        ensureDirExistAndWritable(gradleHome, REPO_CACHE_PATH);
        ensureDirExistAndWritable(gradleHome, GO_BINARAY_CACHE_PATH);
        initialized = true;
    }

    @Override
    public void startSession(VcsGolangPackage pkg) {
        Assert.isNull(sessions.get());
        ensureGlobalCacheExistAndWritable();
        try {
            lock.lock();
            sessions.set(new Session(pkg));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void endSession() {
        try {
            if (sessions.get() == null) {
                return;
            }
            sessions.get().cleanUp();
            sessions.set(null);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void repoUpdated() {
        sessions.get().repositoryMetadata.updated();
    }

    @Override
    public File getGlobalCacheRepoDir() {
        return sessions.get().repoRoot;
    }

    private Optional<GolangRepositoryMetadata> findMatchedRepository(GlobalCacheMetadata metadata,
                                                                     GolangRepository repository) {
        return metadata.getRepositories().stream().filter(repository::match).findFirst();
    }

    @Override
    public File getGlobalGoBinCacheDir(String relativePath) {
        ensureGlobalCacheExistAndWritable();
        return gradleHome.resolve(GO_BINARAY_CACHE_PATH).resolve(relativePath).toFile();
    }

    private File getRepoPath(VcsGolangPackage pkg, String dirName) {
        return gradleHome.resolve(REPO_CACHE_PATH).resolve(pkg.getRootPath()).resolve(dirName).toFile();
    }

    private File getMetadataPath(String packagePath) {
        ensureGlobalCacheExistAndWritable();
        return gradleHome.resolve(REPO_CACHE_PATH).resolve(packagePath).resolve(METADATA_FILE_NAME).toFile();
    }

    private File getMetadataPath(VcsGolangPackage pkg) {
        return getMetadataPath(pkg.getRootPathString());
    }

    private File getMetadataPath(Path path) {
        return getMetadataPath(toUnixString(path));
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
        File lockFile = getMetadataPath(packagePath);
        FileChannel channel = null;
        FileLock fileLock = null;
        try {
            channel = new RandomAccessFile(lockFile, "r").getChannel();
            // Here we must use tryLock to avoid dead-lock
            fileLock = channel.tryLock(0L, Long.MAX_VALUE, true);
            if (fileLock == null) {
                return Optional.empty();
            } else {
                return getMetadataFromFile(channel);
            }
        } catch (FileNotFoundException | OverlappingFileLockException e) {
            return Optional.empty();
        } finally {
            if (fileLock != null) {
                fileLock.release();
            }
            if (channel != null) {
                channel.close();
            }
        }
    }

    @Override
    public boolean currentRepositoryIsUpToDate() {
        long cacheSecond = setting.getGlobalCacheSecond();
        long updateTime = sessions.get().repositoryMetadata.getLastUpdatedTime();
        return System.currentTimeMillis() - updateTime < toMilliseconds(cacheSecond);
    }

    private Optional<GlobalCacheMetadata> getMetadataFromFile(FileChannel fileChannel) throws IOException {
        ByteBuffer buf = ByteBuffer.allocate((int) fileChannel.size());
        fileChannel.position(0);
        fileChannel.read(buf);

        String fileContent = new String(toByteArray(buf), DEFAULT_CHARSET);
        if (isEmpty(fileContent)) {
            return Optional.empty();
        } else {
            return Optional.of(parseYaml(fileContent, GlobalCacheMetadata.class));
        }
    }

    private FileChannel createLockFileIfNecessary(File lockFile) throws IOException {
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
        throw new IOException("Fail to create lock file " + lockFile.getAbsolutePath());
    }
}
