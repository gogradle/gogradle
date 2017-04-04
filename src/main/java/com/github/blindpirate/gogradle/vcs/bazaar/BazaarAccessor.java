package com.github.blindpirate.gogradle.vcs.bazaar;

import com.github.blindpirate.gogradle.vcs.VcsAccessor;

import javax.inject.Singleton;
import java.io.File;
import java.nio.file.Path;

@Singleton
public class BazaarAccessor implements VcsAccessor {
    @Override
    public String getRemoteUrl(File repoRoot) {
        throw new UnsupportedOperationException("Bazaar support is under development now!");
    }

    @Override
    public long lastCommitTimeOfPath(File repoRoot, Path relativePath) {
        throw new UnsupportedOperationException("Bazaar support is under development now!");
    }
}
