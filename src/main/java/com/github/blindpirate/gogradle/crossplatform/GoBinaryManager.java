package com.github.blindpirate.gogradle.crossplatform;

import java.nio.file.Path;

public interface GoBinaryManager {
    String getGoVersion();

    Path getBinaryPath();

    Path getGoroot();
}
