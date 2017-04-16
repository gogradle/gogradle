package com.github.blindpirate.gogradle.core.cache;

import com.github.blindpirate.gogradle.core.dependency.NotationDependency;
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency;
import groovy.lang.Singleton;
import org.gradle.api.Project;

import javax.inject.Inject;

@Singleton
public class ConcreteVcsNotationToResolvedDependencyCache
        extends PersistentCache<NotationDependency, ResolvedDependency> {
    @Inject
    public ConcreteVcsNotationToResolvedDependencyCache(Project project) {
        super(project);
    }
}
