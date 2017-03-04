package com.github.blindpirate.gogradle.vcs.mercurial.client;

import com.github.blindpirate.gogradle.vcs.mercurial.HgRepository;

import java.io.File;

public class HgClientRepository implements HgRepository {
    private File rootDir;

    public static HgClientRepository of(File rootDir) {
        HgClientRepository ret = new HgClientRepository();
        ret.rootDir = rootDir;
        return ret;
    }

    private HgClientRepository() {
    }

    public File getRootDir() {
        return rootDir;
    }
}
