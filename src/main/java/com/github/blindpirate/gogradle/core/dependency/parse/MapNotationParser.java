package com.github.blindpirate.gogradle.core.dependency.parse;

import com.github.blindpirate.gogradle.core.dependency.NotationDependency;

import java.util.Map;

public interface MapNotationParser extends NotationParser<Map<String, Object>> {
    String NAME_KEY = "name";
    String DIR_KEY = "dir";
    String PACKAGE_KEY = "package";
    String VCS_KEY = "vcs";

    @Override
    NotationDependency parse(Map<String, Object> notation);
}
