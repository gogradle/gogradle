package com.github.blindpirate.gogradle.vcs.mercurial;

import com.github.blindpirate.gogradle.core.dependency.AbstractNotationDependency;
import com.github.blindpirate.gogradle.core.dependency.resolve.DependencyResolver;

public class MercurialNotationDependency extends AbstractNotationDependency {
    public MercurialNotationDependency() {
        throw new UnsupportedOperationException("Mercurial is not implemented yet!");
    }

    @Override
    protected Class<? extends DependencyResolver> getResolverClass() {
        throw new UnsupportedOperationException("Mercurial is not implemented yet!");
    }
}
