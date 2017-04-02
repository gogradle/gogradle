package com.github.blindpirate.gogradle.core.dependency;

import java.util.Set;
import java.util.function.Predicate;

public interface NotationDependency extends GolangDependency {
    Set<Predicate<GolangDependency>> getTransitiveDepExclusions();
}
