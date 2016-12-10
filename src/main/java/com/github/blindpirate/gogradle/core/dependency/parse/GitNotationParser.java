package com.github.blindpirate.gogradle.core.dependency.parse;

import com.github.blindpirate.gogradle.core.dependency.GolangDependency;
import com.github.blindpirate.gogradle.util.FactoryUtil;
import com.google.inject.BindingAnnotation;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Map;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Singleton
public class GitNotationParser implements MapNotationParser {

    private final List<? extends MapNotationParser> delegates;

    @Inject
    public GitNotationParser(
            @GitNotationParsers List<? extends MapNotationParser> delegates) {

        this.delegates = delegates;
    }

    @Override
    public boolean accept(Object notation) {
        return notation instanceof Map;
    }

    @Override
    public GolangDependency produce(Object notation) {
        return parseMap((Map<String, Object>) notation);
    }

    @Override
    public GolangDependency parseMap(Map<String, Object> notation) {
        return FactoryUtil.produce(delegates, (Object) notation);
    }

    @BindingAnnotation
    @Target({FIELD, PARAMETER, METHOD})
    @Retention(RUNTIME)
    public @interface GitNotationParsers {
    }
}
