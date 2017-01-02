package com.github.blindpirate.gogradle.core.dependency;

import com.github.blindpirate.gogradle.core.dependency.resolve.DependencyResolver;
import com.github.blindpirate.gogradle.core.pack.LocalFileResolver;

public class LocalDirectoryNotationDependency extends AbstractNotationDependency {
    private String dir;

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    @Override
    protected Class<? extends DependencyResolver> resolverClass() {
        return LocalFileResolver.class;
    }

}
