/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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
             throw DependencyResolutionException.cannotParseNotation(notation);
        }
    }
}
