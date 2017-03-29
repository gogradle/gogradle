package com.github.blindpirate.gogradle.vcs;

import java.io.File;
import java.nio.file.Path;

public interface VcsAccessor {
    String getRemoteUrl(File repoRoot);

    long lastCommitTimeOfPath(File repoRoot, Path relativePath);
}
