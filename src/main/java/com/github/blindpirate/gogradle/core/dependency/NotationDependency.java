package com.github.blindpirate.gogradle.core.dependency;

import java.io.Serializable;
import java.util.Set;
import java.util.function.Predicate;

public interface NotationDependency extends GolangDependency, Serializable {
    Set<Predicate<GolangDependency>> getTransitiveDepExclusions();
}
