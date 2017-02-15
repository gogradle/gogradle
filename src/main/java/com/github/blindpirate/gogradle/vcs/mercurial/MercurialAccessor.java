package com.github.blindpirate.gogradle.vcs.mercurial;

import com.github.blindpirate.gogradle.util.ExceptionHandler;
import com.github.blindpirate.gogradle.vcs.VcsAccessor;
import org.tmatesoft.hg.core.HgRepositoryNotFoundException;
import org.tmatesoft.hg.repo.HgLookup;
import org.tmatesoft.hg.repo.HgRepository;

import javax.inject.Singleton;
import java.io.File;

@Singleton
public class MercurialAccessor implements VcsAccessor {
    @Override
    public String getRemoteUrl(File repoRoot) {
        HgRepository repository = getRepository(repoRoot);
        return getRemoteUrl(repository);
    }

    public String getRemoteUrl(HgRepository repository) {
        return repository.getConfiguration().getStringValue("paths", "default", null);
    }

    public HgRepository getRepository(File repoRoot) {
        try {
            return new HgLookup().detect(repoRoot);
        } catch (HgRepositoryNotFoundException e) {
            throw ExceptionHandler.uncheckException(e);
        }
    }
}
