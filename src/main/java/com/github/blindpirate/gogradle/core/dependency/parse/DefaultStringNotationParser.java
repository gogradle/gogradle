package com.github.blindpirate.gogradle.core.dependency.parse;

import com.github.blindpirate.gogradle.core.dependency.GolangDependency;
import com.github.blindpirate.gogradle.core.dependency.resolve.DependencyResolutionException;
import com.github.blindpirate.gogradle.util.FactoryUtil;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

import static com.github.blindpirate.gogradle.util.FactoryUtil.NoViableFactoryException;

/**
 * Parses a string to a dependency.
 * Currently, only dependency with well-known hosts can be parsed properly.
 */
public class DefaultStringNotationParser implements StringNotationParser {

    private final List<? extends NotationParser> parsers;

    @Inject
    public DefaultStringNotationParser(GithubNotationParser githubNotationParser) {
        this.parsers = Arrays.asList(githubNotationParser);
    }

    @Override
    public GolangDependency produce(Object notation) {
        try {
            return FactoryUtil.produce(parsers, notation);
        } catch (NoViableFactoryException e) {
            throw new DependencyResolutionException("Cannot resolve dependency:" + notation);
        }
    }

    @Override
    public boolean accept(Object notation) {
        return notation instanceof String;
    }

}
