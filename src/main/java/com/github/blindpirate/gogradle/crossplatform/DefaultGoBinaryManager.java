package com.github.blindpirate.gogradle.crossplatform;

public class DefaultGoBinaryManager implements GoBinaryManager {
    @Override
    public String binaryPath() {
        return "go";
    }
}
