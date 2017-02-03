package com.github.blindpirate.gogradle.vcs.mercurial;

import com.github.blindpirate.gogradle.vcs.VcsAccessor;

import javax.inject.Singleton;
import java.io.File;
@Singleton
public class MercurialAccessor implements VcsAccessor {
    @Override
    public String getRemoteUrl(File repoRoot) {
        throw new UnsupportedOperationException("Mercurial support is under development now!");
    }
}
