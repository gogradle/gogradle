package com.github.blindpirate.gogradle.core.dependency.parse;

import com.github.blindpirate.gogradle.core.dependency.GolangDependency;

public interface NotationParser<T> {
    GolangDependency parse(T notation);
}
