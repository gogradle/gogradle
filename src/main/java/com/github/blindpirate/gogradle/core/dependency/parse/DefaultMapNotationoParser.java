package com.github.blindpirate.gogradle.core.dependency.parse;

import com.github.blindpirate.gogradle.core.dependency.GolangDependency;
import com.github.blindpirate.gogradle.util.Assert;
import com.github.blindpirate.gogradle.util.FactoryUtil;
import com.github.blindpirate.gogradle.vcs.VcsType;
import com.google.common.base.Optional;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class DefaultMapNotationoParser implements MapNotationParser {

    private final List<? extends MapNotationParser> parsers;

    @Inject
    public DefaultMapNotationoParser(GithubNotationParser githubNotationParser) {
        parsers = Arrays.asList(githubNotationParser);
    }

    @Override
    public boolean accept(Object notation) {
        return notation instanceof Map;
    }

    @Override
    public GolangDependency produce(Object notation) {
        Map<String, Object> notationMap = (Map<String, Object>) notation;
        ensureNameExist(notationMap);

        if (vcsSpecified(notationMap)) {
            return buildByVcs(notationMap);
        } else {
            return FactoryUtil.produce(parsers, notation);
        }
    }

    private void ensureNameExist(Map<String, ?> notation) {
        Assert.isTrue(notation.containsKey(NAME_KEY), "name must be specified!");
    }

    private GolangDependency buildByVcs(Map<String, ?> notation) {
        VcsType vcs = extractVcsType(notation).get();
        return vcs.getParser().produce(notation);
    }

    private boolean vcsSpecified(Map<String, ?> notation) {
        return extractVcsType(notation).isPresent();
    }

    private Optional<VcsType> extractVcsType(Map<String, ?> notation) {
        Object value = notation.get(VCS_KEY);
        return value == null ? Optional.<VcsType>absent() : VcsType.of(value.toString());
    }

}
