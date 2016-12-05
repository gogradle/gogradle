package com.github.blindpirate.gogradle.vcs;

import java.nio.file.Path;

public interface PackageFetcher {
    void fetch(String packageName, Path location);
}
