package com.github.blindpirate.gogradle.core.dependency.parse;

import java.util.Map;

public interface NotationConverter {
    Map<String, Object> convert(String notation);
}
