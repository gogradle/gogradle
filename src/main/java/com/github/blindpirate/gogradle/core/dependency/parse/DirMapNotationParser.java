package com.github.blindpirate.gogradle.core.dependency.parse;

import com.github.blindpirate.gogradle.core.dependency.LocalDirectoryNotationDependency;
import com.github.blindpirate.gogradle.core.dependency.NotationDependency;

import javax.inject.Singleton;
import java.util.Map;

@Singleton
public class DirMapNotationParser extends AutoConfigureMapNotationParser {
    @Override
    protected Class<? extends NotationDependency> determineDependencyClass(Map<String, Object> notationMap) {
        return LocalDirectoryNotationDependency.class;
    }
}
