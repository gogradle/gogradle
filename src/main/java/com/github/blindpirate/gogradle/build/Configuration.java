package com.github.blindpirate.gogradle.build;

public enum Configuration {

    BUILD("build"),
    TEST("test");

    private String name;

    public String getName() {
        return name;
    }

    Configuration(String name) {
        this.name = name;
    }
}
