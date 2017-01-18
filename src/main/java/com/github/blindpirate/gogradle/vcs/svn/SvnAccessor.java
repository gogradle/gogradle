package com.github.blindpirate.gogradle.vcs.svn;

import com.github.blindpirate.gogradle.vcs.VcsAccessor;

import java.io.File;

public class SvnAccessor implements VcsAccessor {
    @Override
    public String getRemoteUrl(File repoRoot) {
        throw new UnsupportedOperationException("Svn support is under development now!");
    }
}
