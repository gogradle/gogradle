package com.github.blindpirate.gogradle.core.pack;

import com.github.blindpirate.gogradle.core.GolangPackageModule;

public abstract class AbstractPakcageModule implements GolangPackageModule {

    public AbstractPakcageModule(String name) {
        this.name = name;
    }

    private String name;

    @Override
    public String getName() {
        return name;
    }
}
