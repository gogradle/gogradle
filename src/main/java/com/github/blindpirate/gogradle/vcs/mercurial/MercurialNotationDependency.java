package com.github.blindpirate.gogradle.vcs.mercurial;

import com.github.blindpirate.gogradle.core.dependency.resolve.DependencyResolver;
import com.github.blindpirate.gogradle.vcs.GitMercurialNotationDependency;

public class MercurialNotationDependency extends GitMercurialNotationDependency {
    @Override
    protected Class<? extends DependencyResolver> getResolverClass() {
        return MercurialDependencyManager.class;
    }
}
