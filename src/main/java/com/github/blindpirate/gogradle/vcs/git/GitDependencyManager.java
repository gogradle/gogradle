package com.github.blindpirate.gogradle.vcs.git;

import com.github.blindpirate.gogradle.core.cache.GlobalCacheManager;
import com.github.blindpirate.gogradle.core.dependency.produce.DependencyVisitor;
import com.github.blindpirate.gogradle.vcs.GitMercurialAccessor;
import com.github.blindpirate.gogradle.vcs.GitMercurialDependencyManager;
import com.github.blindpirate.gogradle.vcs.VcsType;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class GitDependencyManager extends GitMercurialDependencyManager {
    private final GitClientAccessor gitAccessor;

    @Inject
    public GitDependencyManager(GlobalCacheManager cacheManager,
                                DependencyVisitor dependencyVisitor,
                                GitClientAccessor gitAccessor) {
        super(cacheManager, dependencyVisitor);
        this.gitAccessor = gitAccessor;
    }

    @Override
    protected GitMercurialAccessor getAccessor() {
        return gitAccessor;
    }

    @Override
    protected VcsType getVcsType() {
        return VcsType.GIT;
    }
}
