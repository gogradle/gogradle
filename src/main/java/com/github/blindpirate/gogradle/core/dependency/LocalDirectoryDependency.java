package com.github.blindpirate.gogradle.core.dependency;

import com.github.blindpirate.gogradle.core.pack.DependencyResolver;
import com.github.blindpirate.gogradle.core.pack.LocalFileResolver;

public class LocalDirectoryDependency extends AbstractNotationDependency {

    private String dir;

    public static LocalDirectoryDependency of(String name, String path) {
        LocalDirectoryDependency ret = new LocalDirectoryDependency();
        ret.setName(name);
        ret.dir = path;
        return ret;
    }

    public String getDir() {
        return dir;
    }

    @Override
    protected Class<? extends DependencyResolver> resolverClass() {
        return LocalFileResolver.class;
    }

}
