package com.github.blindpirate.gogradle.core.pack;

import com.github.blindpirate.gogradle.core.dependency.AbstractResolvedDependency;

import java.io.File;
import java.time.Instant;
import java.util.Map;

public class LocalFileSystemDependency extends AbstractResolvedDependency {
    private File rootDir;

    public File getRootDir() {
        return rootDir;
    }

    private LocalFileSystemDependency(String name, String version, long updateTime, File rootDir) {
        super(name, version, updateTime);
        this.rootDir = rootDir;
    }

    public static LocalFileSystemDependency fromLocal(String name, File rootDir) {
        long lastModifiedTime = rootDir.lastModified();
        Instant time = Instant.ofEpochMilli(lastModifiedTime);
        return new LocalFileSystemDependency(name, time.toString(), lastModifiedTime, rootDir);
    }

    @Override
    public Map<String, Object> toLockedNotation() {
        throw new UnsupportedOperationException();
    }

    // version of local directory is its timestamp
    @Override
    public String getVersion() {
        return Instant.ofEpochMilli(getUpdateTime()).toString();
    }
}
