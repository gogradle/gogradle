package com.github.blindpirate.gogradle.vcs;

import com.github.blindpirate.gogradle.core.dependency.parse.NotationParser;
import com.google.common.base.Optional;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;

public enum VcsType {
    Git("git"),
    Mercurial("hg"),
    Svn("svn"),
    Bazaar("bzr");

    private String suffix;

    private static Injector injector;

    public static void setInjector(Injector injector) {
        VcsType.injector = injector;
    }

    public PackageFetcher getFetcher() {
        return injector.getInstance(Key.get(PackageFetcher.class, Names.named(toString())));
    }


    public NotationParser getParser() {
        return injector.getInstance(Key.get(NotationParser.class, Names.named(toString())));
    }

    VcsType(String suffix) {
        this.suffix = suffix;
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
