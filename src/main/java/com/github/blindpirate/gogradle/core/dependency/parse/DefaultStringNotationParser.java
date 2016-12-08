package com.github.blindpirate.gogradle.core.dependency.parse;

import com.github.blindpirate.gogradle.core.dependency.GolangDependency;
import com.github.blindpirate.gogradle.core.dependency.resolve.DependencyResolutionException;
import com.github.blindpirate.gogradle.util.FactoryUtil;
import com.google.inject.BindingAnnotation;

import javax.inject.Inject;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.List;

import static com.github.blindpirate.gogradle.util.FactoryUtil.NoViableFactoryException;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Parses a string to a dependency.
 * Currently, only dependency with well-known hosts can be parsed properly.
 */
public class DefaultStringNotationParser implements StringNotationParser {

    private final List<? extends StringNotationParser> parsers;

    @Inject
    public DefaultStringNotationParser(
            @StringNotationParsers List<? extends StringNotationParser> parsers) {
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
        try {
            return FactoryUtil.produce(parsers, (Object) notation);
        } catch (NoViableFactoryException e) {
            throw new DependencyResolutionException("Cannot resolve dependency:" + notation);
        }
    }

    @BindingAnnotation
    @Target({FIELD, PARAMETER, METHOD})
    @Retention(RUNTIME)
    public @interface StringNotationParsers {
    }
}
