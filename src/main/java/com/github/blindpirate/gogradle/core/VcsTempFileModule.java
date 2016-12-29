package com.github.blindpirate.gogradle.core;

import com.github.blindpirate.gogradle.core.dependency.LockEnabled;

import java.nio.file.Path;
import java.util.Map;

/**
 * Its updateTime is from vcs.
 */
public class VcsTempFileModule extends FileSystemModule implements LockEnabled {

    private Map<String, String> lockNotation;

    public VcsTempFileModule(String name,
                             Path rootDir,
                             long updateTime,
                             Map<String, String> lockNotation) {
        super(name, rootDir, updateTime);
        this.lockNotation = lockNotation;
    }

    @Override
    public FileSystemModule vendor(String packagePath) {
        return null;
    }

    @Override
    public Map<String, String> toLockNotation() {
        return lockNotation;
    }
}
