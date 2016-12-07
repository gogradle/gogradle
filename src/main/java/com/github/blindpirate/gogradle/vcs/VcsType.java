package com.github.blindpirate.gogradle.vcs;

import com.github.blindpirate.gogradle.util.Assert;
import com.google.common.base.Optional;

import java.nio.file.Path;

public enum VcsType implements PackageFetcher {
    Git("git"),
    Mercurial("hg"),
    Svn("svn"),
    Bazaar("bzr");

    private String suffix;

    private PackageFetcher fetcher;

    public void setFetcher(PackageFetcher fetcher) {
        Assert.isTrue(this.fetcher == null, "Fetcher must be inited only once!");
        this.fetcher = fetcher;
    }

    VcsType(String suffix) {
        this.suffix = suffix;
    }


    public void fetch(String packageName, Path location) {
        fetcher.fetch(packageName, location);
    }

    public static Optional<VcsType> of(String name) {
        for (VcsType type : values()) {
            if (type.toString().equals(name) || type.suffix.equals(name)) {
                return Optional.of(type);
            }
        }
        return Optional.absent();
    }

    public static Optional<VcsType> ofDotSuffix(String name) {
        for (VcsType type : values()) {
            if (name.endsWith("." + type.suffix)) {
                return Optional.of(type);
            }
        }
        return Optional.absent();
    }
}
