package com.github.blindpirate.gogradle.core.dependency;

import com.github.blindpirate.gogradle.core.GolangPackage;
import org.gradle.api.artifacts.Dependency;

import java.io.Serializable;

public abstract class AbstractGolangDependency implements GolangDependency, Serializable {
    private String name;
    private boolean firstLevel;

    private GolangPackage golangPackage;

    public GolangPackage getPackage() {
        return golangPackage;
    }

    public void setPackage(GolangPackage golangPackage) {
        this.golangPackage = golangPackage;
    }

    @Override
    public boolean isFirstLevel() {
        return firstLevel;
    }

    public void setFirstLevel(boolean firstLevel) {
        this.firstLevel = firstLevel;
    }

    protected void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getGroup() {
        throw new UnsupportedOperationException("Unsupported method getGroup is invoked!");
    }

    @Override
    public String getVersion() {
        throw new UnsupportedOperationException("Unsupported method getVersion is invoked!");
    }

    @Override
    public boolean contentEquals(Dependency dependency) {
        throw new UnsupportedOperationException("Unsupported method contentEquals is invoked!");
    }

    @Override
    public Dependency copy() {
        throw new UnsupportedOperationException("Unsupported method copy is invoked!");
    }


}
