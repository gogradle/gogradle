package com.github.blindpirate.gogradle.core.dependency;

import com.github.blindpirate.gogradle.core.dependency.parse.MapNotationParser;
import com.github.blindpirate.gogradle.util.Assert;
import com.google.common.collect.ImmutableMap;

import javax.inject.Singleton;
import java.io.File;
import java.util.Map;

@Singleton
public class GogradleRootProject extends LocalDirectoryDependency {
    public static final String GOGRADLE_ROOT = "GOGRADLE_ROOT";

    public void initSingleton(String name, File rootDir) {
        Assert.isTrue(getName() == null, "Gogradle root project can be initialized only once!");
        setName(name);
        setDir(rootDir);
    }

    @Override
    public void setDir(String dir) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResolvedDependency doResolve(ResolveContext context) {
        return this;
    }

    @Override
    public long getUpdateTime() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, Object> toLockedNotation() {
        return ImmutableMap.of(MapNotationParser.NAME_KEY, GOGRADLE_ROOT);
    }


    @Override
    public String formatVersion() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getVersion() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return GOGRADLE_ROOT;
    }

    @Override
    public Object clone() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(Object o) {
        return this == o;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }
}
