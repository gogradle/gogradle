package com.github.blindpirate.gogradle.vcs.mercurial;

import com.github.blindpirate.gogradle.GogradleGlobal;
import com.github.blindpirate.gogradle.core.dependency.ResolveContext;
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency;
import com.github.blindpirate.gogradle.vcs.GitMercurialNotationDependency;

public class MercurialNotationDependency extends GitMercurialNotationDependency {
    @Override
    protected ResolvedDependency doResolve(ResolveContext context) {
        return GogradleGlobal.getInstance(MercurialDependencyManager.class).resolve(context, this);
    }
}
