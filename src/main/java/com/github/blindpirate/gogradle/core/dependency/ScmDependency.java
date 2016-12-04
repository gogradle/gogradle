package com.github.blindpirate.gogradle.core.dependency;

import com.github.blindpirate.gogradle.core.vcs.Vcs;

public abstract class ScmDependency extends AbstractGolangDependency {
    private String name;

    public abstract Vcs vcs();

    public ScmDependency(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    protected void setName(String name) {
        this.name = name;
    }
}
