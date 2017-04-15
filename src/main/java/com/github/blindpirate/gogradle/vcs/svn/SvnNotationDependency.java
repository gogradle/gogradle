package com.github.blindpirate.gogradle.vcs.svn;

import com.github.blindpirate.gogradle.core.dependency.AbstractNotationDependency;
import com.github.blindpirate.gogradle.core.dependency.resolve.DependencyResolver;

public class SvnNotationDependency extends AbstractNotationDependency {
    public SvnNotationDependency() {
        throw new UnsupportedOperationException("Svn is not implemented yet!");
    }

    @Override
    protected Class<? extends DependencyResolver> getResolverClass() {
        throw new UnsupportedOperationException("Svn is not implemented yet!");
    }

    @Override
    public boolean isConcrete() {
        throw new UnsupportedOperationException("Svn is not implemented yet!");
    }
}
