package com.github.blindpirate.gogradle.core.dependency.parse;

import com.github.blindpirate.gogradle.core.dependency.GolangDependency;
import com.github.blindpirate.gogradle.core.dependency.LocalDirectoryDependency;

import javax.inject.Singleton;
import java.util.Map;

@Singleton
public class DirMapNotationParser extends AutoConfigureMapNotationParser {

    @Override
    protected Class<? extends GolangDependency> determineDependencyClass(Map<String, Object> notationMap) {
        return LocalDirectoryDependency.class;
    }
}
