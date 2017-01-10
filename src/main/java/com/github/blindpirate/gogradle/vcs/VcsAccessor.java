package com.github.blindpirate.gogradle.vcs;

import java.io.File;

public interface VcsAccessor {
    String getRemoteUrl(File repoRoot);
}
