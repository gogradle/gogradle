package com.github.blindpirate.gogradle.core.dependency.parse;

import com.github.blindpirate.gogradle.core.dependency.GitDependency;
import com.github.blindpirate.gogradle.core.dependency.GolangDependency;
import com.github.blindpirate.gogradle.util.Cast;
import com.github.blindpirate.gogradle.vcs.VcsType;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

import static com.github.blindpirate.gogradle.vcs.VcsType.Git;

public class VcsMapNotaionParser extends AutoConfigureMapNotationParser {
    private Map<VcsType, Class<? extends GolangDependency>> typeToClassMap =
            ImmutableMap.<VcsType, Class<? extends GolangDependency>>of(
                    Git, GitDependency.class);


    @Override
    public boolean accept(Object notation) {
        return vcsSpecified(Cast.cast(Map.class, notation));
    }

    @Override
    protected Class<? extends GolangDependency> determinDependencyClass(Map<String, Object> notationMap) {
        VcsType type = extractVcsType(notationMap).get();
        return typeToClassMap.get(type);
    }


    private boolean vcsSpecified(Map<String, Object> notation) {
        return extractVcsType(notation).isPresent();
    }

    private Optional<VcsType> extractVcsType(Map<String, ?> notation) {
        Object value = notation.get(VCS_KEY);
        return value == null ? Optional.<VcsType>absent() : VcsType.of(value.toString());
    }
}
