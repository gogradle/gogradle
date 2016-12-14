package com.github.blindpirate.gogradle.core.dependency.parse;

import com.github.blindpirate.gogradle.core.dependency.GolangDependency;
import com.github.blindpirate.gogradle.core.exceptions.DependencyResolutionException;
import com.github.blindpirate.gogradle.util.Assert;
import com.github.blindpirate.gogradle.util.Cast;
import com.github.blindpirate.gogradle.util.FactoryUtil;
import com.github.blindpirate.gogradle.vcs.VcsType;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
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
public class DefaultMapNotationParser implements MapNotationParser {

    private final List<? extends MapNotationParser> delegates;

    @Inject
    public DefaultMapNotationParser(
            @MapNotationParsers List<? extends MapNotationParser> delegates) {
        this.delegates = delegates;
    }

    @Override
    public boolean accept(Object notation) {
        return notation instanceof Map;
    }

    @Override
    public GolangDependency produce(Object notation) {
        return parseMap(Cast.cast(Map.class, notation));
    }

    @Override
    public GolangDependency parseMap(Map<String, Object> notation) {
        Optional<GolangDependency> result = FactoryUtil.<Object, GolangDependency>produce(delegates, notation);
        if (result.isPresent()) {
            return result.get();
        } else {
            throw new DependencyResolutionException("Unable to parse notation:" + notation);
        }
    }

    @BindingAnnotation
    @Target({FIELD, PARAMETER, METHOD})
    @Retention(RUNTIME)
    public @interface MapNotationParsers {
    }

}
