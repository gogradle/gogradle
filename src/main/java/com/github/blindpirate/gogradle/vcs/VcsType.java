package com.github.blindpirate.gogradle.vcs;

import com.github.blindpirate.gogradle.core.dependency.parse.NotationConverter;
import com.google.inject.Injector;
import com.google.inject.Key;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Optional;

public enum VcsType {
    Git("git", ".git", Git.class),
    Mercurial("hg", ".hg", Mercurial.class),
    Svn("svn", ".svn", Svn.class),
    Bazaar("bzr", ".bzr", Bazaar.class);

    private String suffix;

    private static Injector injector;
    private String repo;
    private Class<? extends Annotation> annoClass;

    public static void setInjector(Injector injector) {
        VcsType.injector = injector;
    }

    VcsType(String suffix, String repo, Class annoClass) {
        this.suffix = suffix;
        this.repo = repo;
        this.annoClass = annoClass;
    }

    public static Optional<VcsType> of(String name) {
        return Arrays.stream(values())
                .filter(type -> type.toString().equals(name) || type.suffix.equals(name))
                .findFirst();
    }

    public <T> T getService(Class<T> serviceClass) {
        return injector.getInstance(Key.get(serviceClass, annoClass));
    }

    public String getRepo() {
        return repo;
    }

    public VcsAccessor getAccessor() {
        return getService(VcsAccessor.class);
    }

    public NotationConverter getNotationConverter() {
        return getService(NotationConverter.class);
    }
}
