package com.github.blindpirate.gogradle.core;

import java.util.Set;

public interface BuildConstraintManager {
    void prepareConstraints();

    Set<String> getAllConstraints();

    Set<String> getExtraConstraints();
}
