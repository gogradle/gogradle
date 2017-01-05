package com.github.blindpirate.gogradle.crossplatform;

import javax.inject.Singleton;

@Singleton
public class DefaultGoBinaryManager implements GoBinaryManager {
    @Override
    public String binaryPath() {
        return "go";
    }

    @Override
    public GoBinary getGoBinary() {
        return null;
    }
}
