package com.github.blindpirate.gogradle.vcs.git;

import com.github.blindpirate.gogradle.GogradleGlobal;
import com.github.blindpirate.gogradle.core.dependency.ResolveContext;
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency;
import com.github.blindpirate.gogradle.vcs.GitMercurialNotationDependency;

public class GitNotationDependency extends GitMercurialNotationDependency {
    @Override
    protected ResolvedDependency doResolve(ResolveContext context) {
        return GogradleGlobal.getInstance(GitDependencyManager.class).resolve(context, this);
    }
}
