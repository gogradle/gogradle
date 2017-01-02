package com.github.blindpirate.gogradle.core.dependency.parse;

import com.github.blindpirate.gogradle.core.dependency.NotationDependency;
import com.github.blindpirate.gogradle.util.ConfigureUtils;

import java.util.Map;

public abstract class AutoConfigureMapNotationParser implements MapNotationParser {
    @Override
    public NotationDependency parse(Map<String, Object> notationMap) {
        try {
            preConfigure(notationMap);

            Class<? extends NotationDependency> resultClass = determineDependencyClass(notationMap);
            NotationDependency ret = resultClass.newInstance();
            ConfigureUtils.configureByMapQuietly(notationMap, ret);

            postConfigure(notationMap, ret);
            return ret;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalStateException("Dependency class must have an accessible default constructor!");
        }
    }

    protected void preConfigure(Map<String, Object> notationMap) {
    }

    protected void postConfigure(Map<String, Object> notationMap, NotationDependency ret) {
    }

    protected abstract Class<? extends NotationDependency> determineDependencyClass(Map<String, Object> notationMap);
}
