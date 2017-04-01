package com.github.blindpirate.gogradle.vcs.mercurial;

import com.github.blindpirate.gogradle.core.cache.GlobalCacheManager;
import com.github.blindpirate.gogradle.core.dependency.produce.DependencyVisitor;
import com.github.blindpirate.gogradle.vcs.GitMercurialAccessor;
import com.github.blindpirate.gogradle.vcs.GitMercurialDependencyManager;
import com.github.blindpirate.gogradle.vcs.VcsType;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class MercurialDependencyManager extends GitMercurialDependencyManager {
    public static final String DEFAULT_BRANCH = "default";

    private final HgClientAccessor accessor;

    @Inject
    public MercurialDependencyManager(HgClientAccessor accessor,
                                      DependencyVisitor visitor,
                                      GlobalCacheManager cacheManager) {
        super(cacheManager, visitor);
        this.accessor = accessor;
    }

    @Override
    protected GitMercurialAccessor getAccessor() {
        return accessor;
    }

    @Override
    protected VcsType getVcsType() {
        return VcsType.MERCURIAL;
    }
}
