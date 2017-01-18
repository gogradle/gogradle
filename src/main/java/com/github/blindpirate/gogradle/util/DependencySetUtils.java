package com.github.blindpirate.gogradle.util;

import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet;
import com.github.blindpirate.gogradle.core.dependency.parse.MapNotationParser;

import java.util.Collection;
import java.util.Map;

public class DependencySetUtils {
    public static GolangDependencySet parseMany(Collection<? extends Map> notations, MapNotationParser parser) {
        GolangDependencySet ret = new GolangDependencySet();
        for (Map notation : notations) {
            ret.add(parser.parse(notation));
        }
        return ret;
    }
}
