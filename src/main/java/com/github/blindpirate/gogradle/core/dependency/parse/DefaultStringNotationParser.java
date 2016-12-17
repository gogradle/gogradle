package com.github.blindpirate.gogradle.core.dependency.parse;

import com.github.blindpirate.gogradle.core.dependency.GolangDependency;
import com.github.blindpirate.gogradle.core.exceptions.DependencyResolutionException;
import com.github.blindpirate.gogradle.util.FactoryUtil;
import com.google.common.base.Optional;
import com.google.inject.BindingAnnotation;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.List;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Parses a string to a dependency.
 * Currently, only dependency with well-known hosts can be parsed properly.
 */
@Singleton
public class DefaultStringNotationParser implements StringNotationParser {

    private final List<StringNotationParser> parsers;

    @Inject
    public DefaultStringNotationParser(
            @StringNotationParsers List<StringNotationParser> parsers) {
        this.parsers = parsers;
    }

    @Override
    public GolangDependency produce(Object notation) {
        return parseString((String) notation);
    }

    @Override
    public boolean accept(Object notation) {
        return notation instanceof String;
    }

    @Override
    public GolangDependency parseString(String notation) {
        Optional<GolangDependency> ret = FactoryUtil.produce(parsers, (Object) notation);
        if (ret.isPresent()) {
            return ret.get();
        } else {
            throw new DependencyResolutionException("Cannot resolve dependency:" + notation);
        }
    }

    @BindingAnnotation
    @Target({FIELD, PARAMETER, METHOD})
    @Retention(RUNTIME)
    public @interface StringNotationParsers {
    }
}
