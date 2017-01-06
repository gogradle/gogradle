package com.github.blindpirate.gogradle.core.dependency;

import com.github.blindpirate.gogradle.core.dependency.resolve.DependencyResolver;
import com.github.blindpirate.gogradle.core.dependency.resolve.LocalDirectoryResolver;

public class LocalDirectoryNotationDependency extends AbstractNotationDependency {
    private String dir;

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    @Override
    public Class<? extends DependencyResolver> getResolverClass() {
        return LocalDirectoryResolver.class;
    }

}
