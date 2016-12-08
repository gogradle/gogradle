package com.github.blindpirate.gogradle.core.dependency.parse;

import com.github.blindpirate.gogradle.core.dependency.GolangDependency;

public interface StringNotationParser extends NotationParser {
    GolangDependency parseString(String notation);
}
