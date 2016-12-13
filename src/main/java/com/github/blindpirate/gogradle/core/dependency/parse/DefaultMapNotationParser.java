package com.github.blindpirate.gogradle.core.dependency.parse;

import com.github.blindpirate.gogradle.core.dependency.GolangDependency;
import com.github.blindpirate.gogradle.util.Assert;
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
        Map<String, Object> notationMap = (Map<String, Object>) notation;
        return parseMap(notationMap);
    }

    private void ensureNameExist(Map<String, ?> notation) {
        Assert.isTrue(notation.containsKey(NAME_KEY), "name must be specified!");
    }

    private GolangDependency buildByVcs(Map<String, Object> notation) {
        VcsType vcs = extractVcsType(notation).get();
        return vcs.getParser().produce(notation);
    }

    private boolean vcsSpecified(Map<String, Object> notation) {
        return extractVcsType(notation).isPresent();
    }

    private Optional<VcsType> extractVcsType(Map<String, ?> notation) {
        Object value = notation.get(VCS_KEY);
        return value == null ? Optional.<VcsType>absent() : VcsType.of(value.toString());
    }

    @Override
    public GolangDependency parseMap(Map<String, Object> notation) {
        ensureNameExist(notation);

        if (vcsSpecified(notation)) {
            return buildByVcs(notation);
        } else {
            return FactoryUtil.<Object, GolangDependency>produce(delegates, notation).get();
        }
    }

    @BindingAnnotation
    @Target({FIELD, PARAMETER, METHOD})
    @Retention(RUNTIME)
    public @interface MapNotationParsers {
    }

}
