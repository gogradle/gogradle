package com.github.blindpirate.gogradle.crossplatform;

public interface GoBinaryManager {
    String getGoVersion();

    String getBinaryPath();

    String getGorootEnv();
}
