package com.github.blindpirate.gogradle.core.dependency;

public abstract class ScmPackageDependency implements GolangPackageDependency {
    private String name;

    public ScmPackageDependency(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public abstract String getVersion();


}
