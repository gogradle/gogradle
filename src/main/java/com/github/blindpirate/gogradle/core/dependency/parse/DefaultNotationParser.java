package com.github.blindpirate.gogradle.core.dependency.parse;

import com.github.blindpirate.gogradle.core.dependency.GolangDependency;
import com.github.blindpirate.gogradle.core.exceptions.DependencyResolutionException;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;

@Singleton
public class DefaultNotationParser implements NotationParser<Object> {

    private final MapNotationParser mapNotationParser;

    private final NotationConverter notationConverter;

    @Inject
    public DefaultNotationParser(MapNotationParser mapNotationParser, NotationConverter notationConverter) {
        this.mapNotationParser = mapNotationParser;
        this.notationConverter = notationConverter;
    }

    @Override
    public GolangDependency parse(Object notation) {
        Map<String, Object> notationMap = convertToMap(notation);
        return mapNotationParser.parse(notationMap);
    }

    private Map<String, Object> convertToMap(Object notation) {
        if (notation instanceof String) {
            return notationConverter.convert((String) notation);
        } else if (notation instanceof Map) {
            return (Map<String, Object>) notation;
        } else {
            throw new DependencyResolutionException("Unable to parse notation of class: " + notation.getClass());
        }
    }
}
