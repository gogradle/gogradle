package com.github.blindpirate.gogradle.vcs.git;

import com.github.blindpirate.gogradle.GolangRepositoryHandler;
import com.github.blindpirate.gogradle.core.cache.GlobalCacheManager;
import com.github.blindpirate.gogradle.core.dependency.produce.DependencyVisitor;
import com.github.blindpirate.gogradle.vcs.Git;
import com.github.blindpirate.gogradle.vcs.GitMercurialAccessor;
import com.github.blindpirate.gogradle.vcs.GitMercurialDependencyManager;

import javax.inject.Singleton;

@Singleton
public class GitDependencyManager extends GitMercurialDependencyManager {

    public static final String DEFAULT_BRANCH = "master";

    private final GitClientAccessor gitAccessor;

    public GitDependencyManager(GlobalCacheManager cacheManager,
                                DependencyVisitor dependencyVisitor,
                                GitClientAccessor gitAccessor,
                                @Git GolangRepositoryHandler golangRepositoryHandler) {
        super(cacheManager, dependencyVisitor, golangRepositoryHandler);
        this.gitAccessor = gitAccessor;
    }

    @Override
    protected String getDefaultBranchName() {
        return DEFAULT_BRANCH;
    }

    @Override
    protected GitMercurialAccessor getAccessor() {
        return gitAccessor;
    }
}
