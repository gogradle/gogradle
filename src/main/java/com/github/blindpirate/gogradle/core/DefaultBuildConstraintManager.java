package com.github.blindpirate.gogradle.core;

import java.util.HashSet;
import java.util.Set;

public class DefaultBuildConstraintManager implements BuildConstraintManager {
    @Override
    public Set<String> getCtx() {
        return new HashSet<>();
    }
}
