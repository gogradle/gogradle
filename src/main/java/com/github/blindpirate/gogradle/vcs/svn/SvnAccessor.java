package com.github.blindpirate.gogradle.vcs.svn;

import com.github.blindpirate.gogradle.vcs.VcsAccessor;

import javax.inject.Singleton;
import java.io.File;
@Singleton
public class SvnAccessor implements VcsAccessor {
    @Override
    public String getRemoteUrl(File repoRoot) {
        throw new UnsupportedOperationException("Svn support is under development now!");
    }
}
