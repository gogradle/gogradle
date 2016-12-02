package com.github.blindpirate.gogradle.core.cache;

import com.github.blindpirate.gogradle.util.FileUtils;
import org.gradle.wrapper.GradleUserHomeLookup;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class DefaultCacheDirectoryManager implements CacheDirectoryManager {
    private static final String GOPATH_CACHE_PATH = "go/gopath";
    private static final String GO_BINARAY_CACHE_PATH = "go/binary";
    private File gradleHome = GradleUserHomeLookup.gradleUserHome();

    @Override
    public void ensureGlobalCacheExistAndWritable() {
        ensureExistAndWritable(GOPATH_CACHE_PATH);
        ensureExistAndWritable(GO_BINARAY_CACHE_PATH);
    }

    private void ensureExistAndWritable(String path) {
        String fullPath = gradleHome.getAbsolutePath() + File.separator + path;
        File dir = new File(fullPath);
        try {
            FileUtils.forceMkdir(dir);
        } catch (IOException e) {
            throw new RuntimeException("Create cache directory "
                    + fullPath
                    + " failed, please check if you have access to it.");
        }

        if (!Files.isWritable(dir.toPath())) {
            throw new RuntimeException("Cannot write to directory:" + fullPath);
        }
    }

    @Override
    public void ensureProjectBuildCacheExist() {
    }
}
