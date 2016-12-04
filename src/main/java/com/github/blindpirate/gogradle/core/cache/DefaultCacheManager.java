package com.github.blindpirate.gogradle.core.cache;

import org.gradle.wrapper.GradleUserHomeLookup;

import java.nio.file.Path;

import static com.github.blindpirate.gogradle.util.FileUtils.ensureExsitAndWritable;

public class DefaultCacheManager implements CacheManager {
    private static final String GOPATH_CACHE_PATH = "go/gopath";
    private static final String GO_BINARAY_CACHE_PATH = "go/binary";
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
}
