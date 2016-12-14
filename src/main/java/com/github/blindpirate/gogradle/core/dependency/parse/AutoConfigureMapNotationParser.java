package com.github.blindpirate.gogradle.core.dependency.parse;

import com.github.blindpirate.gogradle.core.dependency.GolangDependency;
import com.github.blindpirate.gogradle.util.Assert;
import com.github.blindpirate.gogradle.util.Cast;
import com.github.blindpirate.gogradle.util.ConfigureUtils;

import java.util.Map;

public abstract class AutoConfigureMapNotationParser implements MapNotationParser {

    @Override
    public GolangDependency produce(Object notation) {
        return parseMap(Cast.cast(Map.class, notation));
    }

    @Override
    public GolangDependency parseMap(Map<String, Object> notationMap) {
        try {
            preConfigure(notationMap);

            Class<? extends GolangDependency> resultClass = determinDependencyClass(notationMap);
            GolangDependency ret = resultClass.newInstance();
            ConfigureUtils.configureByMapQuietly(notationMap, ret);

            postConfigure(notationMap, ret);
            return ret;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalStateException("Dependency class must have a default constructor!");
        }
    }

    protected void preConfigure(Map<String, Object> notationMap) {
        Assert.isTrue(notationMap.containsKey(NAME_KEY), "Name must be specified!");
    }

    protected void postConfigure(Map<String, Object> notationMap, GolangDependency ret) {
    }

    protected abstract Class<? extends GolangDependency> determinDependencyClass(Map<String, Object> notationMap);
}
