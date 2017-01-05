package com.github.blindpirate.gogradle.vcs;

import java.io.File;
import java.util.List;

public interface VcsAccessor {
    List<String> getRemoteUrls(File repoRoot);
}
