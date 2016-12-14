package com.github.blindpirate.gogradle.core.dependency.parse;

import com.github.blindpirate.gogradle.core.dependency.GolangDependency;
import com.github.blindpirate.gogradle.core.dependency.LocalDirectoryDependency;
import com.github.blindpirate.gogradle.util.Assert;
import com.github.blindpirate.gogradle.util.Cast;
import com.github.blindpirate.gogradle.util.MapUtils;

import javax.inject.Singleton;
import java.util.Map;

@Singleton
public class DirMapNotationParser extends AutoConfigureMapNotationParser {
    private static final String PATH_KEY = "path";

    @Override
    public boolean accept(Object notation) {
        return Cast.cast(Map.class, notation).containsKey(DIR_KEY);
    }

    @Override
    protected void preConfigure(Map<String, Object> notationMap) {
        super.preConfigure(notationMap);
        Assert.isTrue(notationMap.containsKey(DIR_KEY), "Dir must be specified!");

        notationMap.put(PATH_KEY, MapUtils.getString(notationMap, DIR_KEY));
    }

    @Override
    protected Class<? extends GolangDependency> determinDependencyClass(Map<String, Object> notationMap) {
        return LocalDirectoryDependency.class;
    }

}
