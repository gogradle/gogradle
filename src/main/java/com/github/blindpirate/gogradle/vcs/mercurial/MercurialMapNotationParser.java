package com.github.blindpirate.gogradle.vcs.mercurial;

import com.github.blindpirate.gogradle.core.dependency.NotationDependency;
import com.github.blindpirate.gogradle.core.dependency.parse.AutoConfigureMapNotationParser;

import javax.inject.Singleton;
import java.util.Map;

@Singleton
public class MercurialMapNotationParser extends AutoConfigureMapNotationParser<NotationDependency> {
    @Override
    protected Class<? extends NotationDependency> determineDependencyClass(Map<String, Object> notationMap) {
        throw new UnsupportedOperationException("Mercurial support is under development now!");
    }
}
