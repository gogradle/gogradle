package com.github.blindpirate.gogradle.vcs.bazaar;

import com.github.blindpirate.gogradle.vcs.VcsAccessor;

import java.io.File;

public class BazaarAccessor implements VcsAccessor {
    @Override
    public String getRemoteUrl(File repoRoot) {
        throw new UnsupportedOperationException("Bazaar support is under development now!");
    }
}
