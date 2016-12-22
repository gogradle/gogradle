package com.github.blindpirate.gogradle.vcs;

import java.nio.file.Path;
import java.util.List;

public interface VcsAccessor {
    List<String> getRemoteUrls(Path repoRoot);
}
