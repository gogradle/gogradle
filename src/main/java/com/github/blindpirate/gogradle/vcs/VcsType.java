package com.github.blindpirate.gogradle.vcs;

import com.google.common.base.Optional;

public enum VcsType {
    Git("git"),
    Mercurial("hg"),
    Svn("svn"),
    Bazaar("bzr");

    private String suffix;

    VcsType(String suffix) {
        this.suffix = suffix;
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
