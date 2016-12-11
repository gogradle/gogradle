package com.github.blindpirate.gogradle.core.dependency.parse;

import com.github.blindpirate.gogradle.core.dependency.GolangDependency;
import com.github.blindpirate.gogradle.core.exceptions.DependencyResolutionException;
import com.github.blindpirate.gogradle.util.FactoryUtil;
import com.google.inject.BindingAnnotation;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.List;

import static com.github.blindpirate.gogradle.util.FactoryUtil.NoViableFactoryException;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Singleton
public class DefaultGolangDependencyParser implements NotationParser {

    private final List<? extends NotationParser> delegates;

    @Inject
    public DefaultGolangDependencyParser(
            @GolangDependencyNotationParsers List<? extends NotationParser> delegates) {
        this.delegates = delegates;
    }


    @Override
    public boolean accept(Object notation) {
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public GolangDependency produce(Object notation) {
        try {
            return FactoryUtil.<Object, GolangDependency>produce(delegates, notation).get();
        } catch (NoViableFactoryException e) {
            throw new DependencyResolutionException("Unsupported dependency notation:" + notation.getClass());
        }
    }

    @BindingAnnotation
    @Target({FIELD, PARAMETER, METHOD})
    @Retention(RUNTIME)
    public @interface GolangDependencyNotationParsers {
    }
}
