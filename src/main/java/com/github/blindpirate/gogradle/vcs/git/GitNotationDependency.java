package com.github.blindpirate.gogradle.vcs.git;

import com.github.blindpirate.gogradle.core.dependency.resolve.DependencyResolver;
import com.github.blindpirate.gogradle.vcs.GitMercurialNotationDependency;

public class GitNotationDependency extends GitMercurialNotationDependency {
    @Override
    protected Class<? extends DependencyResolver> getResolverClass() {
        return GitDependencyManager.class;
    }
}
