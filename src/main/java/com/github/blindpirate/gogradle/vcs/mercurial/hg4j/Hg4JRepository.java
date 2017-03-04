package com.github.blindpirate.gogradle.vcs.mercurial.hg4j;

import com.github.blindpirate.gogradle.vcs.mercurial.HgRepository;

public class Hg4JRepository implements HgRepository {
    private org.tmatesoft.hg.repo.HgRepository underlyingRepository;

    public static Hg4JRepository fromUnderlying(org.tmatesoft.hg.repo.HgRepository repo) {
        return new Hg4JRepository(repo);
    }

    private Hg4JRepository(org.tmatesoft.hg.repo.HgRepository underlyingRepository) {
        this.underlyingRepository = underlyingRepository;
    }

    public org.tmatesoft.hg.repo.HgRepository getUnderlying() {
        return underlyingRepository;
    }
}
