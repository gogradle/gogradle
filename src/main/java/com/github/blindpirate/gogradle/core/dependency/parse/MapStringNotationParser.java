package com.github.blindpirate.gogradle.core.dependency.parse;

import com.github.blindpirate.gogradle.core.dependency.GolangDependency;

import java.util.Map;

public abstract class MapStringNotationParser implements MapNotationParser, StringNotationParser {
    @Override
    public boolean accept(Object notation) {
        if (notation instanceof String) {
            return acceptString((String) notation);
        } else if (notation instanceof Map) {
            return acceptMap((Map<String, Object>) notation);
        }
        return false;
    }

    protected abstract boolean acceptMap(Map<String, Object> notation);

    protected abstract boolean acceptString(String notation);

    @Override
    public GolangDependency produce(Object notation) {
        if (notation instanceof String) {
            return parseString((String) notation);
        } else {
            return parseMap((Map<String, Object>) notation);
        }
    }

    protected abstract GolangDependency parseMap(Map<String, Object> notation);

    protected abstract GolangDependency parseString(String notation);
}
