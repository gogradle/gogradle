package com.github.blindpirate.gogradle.vcs.svn;

import com.github.blindpirate.gogradle.core.dependency.parse.NotationConverter;

import java.util.Map;

public class SvnNotationConverter implements NotationConverter {
    @Override
    public Map<String, Object> convert(String notation) {
        throw new UnsupportedOperationException("Svn support is under development now!");
    }
}
