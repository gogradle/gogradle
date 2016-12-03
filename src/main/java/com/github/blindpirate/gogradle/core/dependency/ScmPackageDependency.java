package com.github.blindpirate.gogradle.core.dependency;

import com.github.blindpirate.gogradle.core.GolangPackageModule;

public abstract class ScmPackageDependency implements GolangPackageDependency {
    private String name;

    public String getName() {
        return name;
    }

    public abstract String getVersion();


}
