package com.github.blindpirate.gogradle.vcs.svn;

import com.github.blindpirate.gogradle.core.dependency.parse.NotationConverter;

import javax.inject.Singleton;
import java.util.Map;
@Singleton
public class SvnNotationConverter implements NotationConverter {
    @Override
    public Map<String, Object> convert(String notation) {
        throw new UnsupportedOperationException("Svn support is under development now!");
    }
}
