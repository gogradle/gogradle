package com.github.blindpirate.gogradle.core.dependency.parse;

import com.github.blindpirate.gogradle.core.dependency.GolangDependency;
import com.github.blindpirate.gogradle.core.dependency.resolve.DependencyResolutionException;
import com.github.blindpirate.gogradle.util.FactoryUtil;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

import static com.github.blindpirate.gogradle.util.FactoryUtil.NoViableFactoryException;

public class DefaultGolangDependencyParser implements NotationParser {

    private final List<NotationParser> factories;

    @Inject
    public DefaultGolangDependencyParser(DefaultStringNotationParser stringNotationParser,
                                         DefaultMapNotationoParser mapNotationParser) {
        this.factories = Arrays.asList(stringNotationParser, mapNotationParser);
    }


    @Override
    public boolean accept(Object notation) {
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public GolangDependency produce(Object notation) {
        try {
            return FactoryUtil.produce(factories, notation);
        } catch (NoViableFactoryException e) {
            throw new DependencyResolutionException("Unsupported dependency notation:" + notation.getClass());
        }
    }
}
