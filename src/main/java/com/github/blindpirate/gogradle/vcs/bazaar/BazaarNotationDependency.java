package com.github.blindpirate.gogradle.vcs.bazaar;

import com.github.blindpirate.gogradle.core.dependency.AbstractNotationDependency;
import com.github.blindpirate.gogradle.core.dependency.resolve.DependencyResolver;

public class BazaarNotationDependency extends AbstractNotationDependency {
    public BazaarNotationDependency() {
        throw new UnsupportedOperationException("Bazaar is not implemented yet!");
    }

    @Override
    protected Class<? extends DependencyResolver> getResolverClass() {
        throw new UnsupportedOperationException("Bazaar is not implemented yet!");
    }
}
