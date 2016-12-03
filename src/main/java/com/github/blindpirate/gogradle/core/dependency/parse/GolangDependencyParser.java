package com.github.blindpirate.gogradle.core.dependency.parse;

import com.github.blindpirate.gogradle.core.dependency.GolangDependency;

public interface GolangDependencyParser {
    GolangDependency parseNotation(Object notaion);
}
