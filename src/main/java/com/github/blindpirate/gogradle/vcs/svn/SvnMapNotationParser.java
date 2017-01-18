package com.github.blindpirate.gogradle.vcs.svn;

import com.github.blindpirate.gogradle.core.dependency.NotationDependency;
import com.github.blindpirate.gogradle.core.dependency.parse.AutoConfigureMapNotationParser;

import java.util.Map;

public class SvnMapNotationParser extends AutoConfigureMapNotationParser {
    @Override
    protected Class<? extends NotationDependency> determineDependencyClass(Map<String, Object> notationMap) {
        throw new UnsupportedOperationException("Svn support is under development now!");
    }
}
