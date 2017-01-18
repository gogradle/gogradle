package com.github.blindpirate.gogradle.vcs.mercurial;

import com.github.blindpirate.gogradle.core.dependency.parse.NotationConverter;

import java.util.Map;

public class MercurialNotationConverter implements NotationConverter {
    @Override
    public Map<String, Object> convert(String notation) {
        throw new UnsupportedOperationException("Mercurial support is under development now!");
    }
}
