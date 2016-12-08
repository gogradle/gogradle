package com.github.blindpirate.gogradle.core.dependency.parse;

import com.github.blindpirate.gogradle.core.dependency.GolangDependency;

import java.util.Map;

public interface MapNotationParser extends NotationParser {
    String VCS_KEY = "vcs";
    String NAME_KEY = "name";

    GolangDependency parseMap(Map<String,Object> notation);

}
