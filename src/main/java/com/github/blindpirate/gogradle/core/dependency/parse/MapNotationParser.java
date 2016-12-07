package com.github.blindpirate.gogradle.core.dependency.parse;

import com.github.blindpirate.gogradle.core.dependency.GolangDependency;

public interface MapNotationParser extends NotationParser {
    String VCS_KEY = "vcs";
    String NAME_KEY = "name";

    @Override
    boolean accept(Object notation);

    @Override
    GolangDependency produce(Object notation);
}
