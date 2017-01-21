package com.github.blindpirate.gogradle.vcs.mercurial;

import com.github.blindpirate.gogradle.core.dependency.parse.NotationConverter;

import javax.inject.Singleton;
import java.util.Map;
@Singleton
public class MercurialNotationConverter implements NotationConverter {
    @Override
    public Map<String, Object> convert(String notation) {
        throw new UnsupportedOperationException("Mercurial support is under development now!");
    }
}
