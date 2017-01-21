package com.github.blindpirate.gogradle.vcs.bazaar;

import com.github.blindpirate.gogradle.core.dependency.NotationDependency;
import com.github.blindpirate.gogradle.core.dependency.parse.AutoConfigureMapNotationParser;

import javax.inject.Singleton;
import java.util.Map;
@Singleton
public class BazaarMapNotationParser extends AutoConfigureMapNotationParser {
    @Override
    protected Class<? extends NotationDependency> determineDependencyClass(Map<String, Object> notationMap) {
        throw new UnsupportedOperationException("Bazaar support is under development now!");
    }
}
