package com.github.blindpirate.gogradle.core.dependency;

import java.util.Map;

/**
 * Represents a dependency which can be locked.
 */
public interface LockEnabled {
    Map<String, String> toLockNotation();
}
