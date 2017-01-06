package com.github.blindpirate.gogradle.core.pack;

import com.github.blindpirate.gogradle.core.dependency.AbstractResolvedDependency;
import com.github.blindpirate.gogradle.core.dependency.resolve.DependencyResolver;
import com.github.blindpirate.gogradle.core.dependency.resolve.LocalDirectoryResolver;

import java.io.File;
import java.time.Instant;
import java.util.Map;

public class LocalDirectoryDependency extends AbstractResolvedDependency {
    private File rootDir;

    public File getRootDir() {
        return rootDir;
    }

    private LocalDirectoryDependency(String name, String version, long updateTime, File rootDir) {
        super(name, version, updateTime);
        this.rootDir = rootDir;
    }

    public static LocalDirectoryDependency fromLocal(String name, File rootDir) {
        long lastModifiedTime = rootDir.lastModified();
        Instant time = Instant.ofEpochMilli(lastModifiedTime);
        return new LocalDirectoryDependency(name, time.toString(), lastModifiedTime, rootDir);
    }

    @Override
    public Map<String, Object> toLockedNotation() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Class<? extends DependencyResolver> getResolverClass() {
        return LocalDirectoryResolver.class;
    }

    // version of local directory is its timestamp
    @Override
    public String getVersion() {
        return Instant.ofEpochMilli(getUpdateTime()).toString();
    }
}
