package com.github.blindpirate.gogradle.core.dependency.parse;

import com.github.blindpirate.gogradle.core.dependency.GolangDependency;

import java.util.Map;

public interface MapNotationParser extends NotationParser<Map<String, Object>> {
    String NAME_KEY = "name";
    String DIR_KEY = "dir";
    String INFO_KEY = "info";

    @Override
    GolangDependency parse(Map<String, Object> notation);

}
