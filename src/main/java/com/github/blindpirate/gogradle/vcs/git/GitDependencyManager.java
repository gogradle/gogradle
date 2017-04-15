package com.github.blindpirate.gogradle.vcs.git;

import com.github.blindpirate.gogradle.core.cache.GlobalCacheManager;
import com.github.blindpirate.gogradle.core.cache.ProjectCacheManager;
import com.github.blindpirate.gogradle.vcs.GitMercurialAccessor;
import com.github.blindpirate.gogradle.vcs.GitMercurialDependencyManager;
import com.github.blindpirate.gogradle.vcs.VcsType;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class GitDependencyManager extends GitMercurialDependencyManager {
    private final GitClientAccessor gitAccessor;

    @Inject
    public GitDependencyManager(GlobalCacheManager globalCacheManager,
                                ProjectCacheManager projectCacheManager,
                                GitClientAccessor gitAccessor) {
        super(globalCacheManager, projectCacheManager);
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
