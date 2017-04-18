package com.github.blindpirate.gogradle.vcs.bazaar;

import com.github.blindpirate.gogradle.core.cache.CacheScope;
import com.github.blindpirate.gogradle.core.dependency.AbstractNotationDependency;
import com.github.blindpirate.gogradle.core.dependency.ResolveContext;
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency;

public class BazaarNotationDependency extends AbstractNotationDependency {
    public BazaarNotationDependency() {
        throw new UnsupportedOperationException("Bazaar is not implemented yet!");
    }

    @Override
    protected ResolvedDependency doResolve(ResolveContext context) {
        throw new UnsupportedOperationException("Bazaar is not implemented yet!");
    }

    @Override
    public CacheScope getCacheScope() {
        throw new UnsupportedOperationException("Bazaar is not implemented yet!");
    }
}
