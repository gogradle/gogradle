package com.github.blindpirate.gogradle.core.cache;

import com.github.blindpirate.gogradle.core.dependency.NotationDependency;
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency;
import groovy.lang.Singleton;
import org.gradle.api.Project;

import javax.inject.Inject;
import java.io.File;

@Singleton
public class PersistenceNotationToResolvedCache
        extends PersistentCache<NotationDependency, ResolvedDependency> {

    @Inject
    public PersistenceNotationToResolvedCache(Project project) {
        super(new File(project.getRootDir(), ".gogradle/cache/PersistenceNotationToResolvedCache.bin"));
    }
}
