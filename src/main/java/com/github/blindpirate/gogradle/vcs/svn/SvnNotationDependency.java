package com.github.blindpirate.gogradle.vcs.svn;

import com.github.blindpirate.gogradle.core.cache.CacheScope;
import com.github.blindpirate.gogradle.core.dependency.AbstractNotationDependency;
import com.github.blindpirate.gogradle.core.dependency.ResolveContext;
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency;

public class SvnNotationDependency extends AbstractNotationDependency {
    public SvnNotationDependency() {
        throw new UnsupportedOperationException("Svn is not implemented yet!");
    }

    @Override
    protected ResolvedDependency doResolve(ResolveContext context) {
        throw new UnsupportedOperationException("Svn is not implemented yet!");
    }

    @Override
    public CacheScope getCacheScope() {
        throw new UnsupportedOperationException("Svn is not implemented yet!");
    }
}
