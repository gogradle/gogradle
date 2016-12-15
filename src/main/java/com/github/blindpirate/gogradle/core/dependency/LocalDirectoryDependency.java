package com.github.blindpirate.gogradle.core.dependency;

import com.github.blindpirate.gogradle.core.pack.DependencyResolver;
import com.github.blindpirate.gogradle.core.pack.LocalFileResolver;

public class LocalDirectoryDependency extends AbstractNotationDependency {

    private String path;

    public static LocalDirectoryDependency of(String name, String path) {
        LocalDirectoryDependency ret = new LocalDirectoryDependency();
        ret.setName(name);
        ret.path = path;
        return ret;
    }

    public String getPath() {
        return path;
    }

    @Override
    protected Class<? extends DependencyResolver> resolverClass() {
        return LocalFileResolver.class;
    }

}
