package com.github.blindpirate.gogradle.core.cache;

import com.github.blindpirate.gogradle.core.dependency.NotationDependency;
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency;

import javax.inject.Singleton;

@Singleton
public class BuildScopedVcsNotationCache extends AbstractCache<NotationDependency, ResolvedDependency> {
}
