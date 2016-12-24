package com.github.blindpirate.gogradle.vcs;

import com.github.blindpirate.gogradle.core.dependency.parse.NotationConverter;
import java.util.Optional;
import com.google.inject.Injector;
import com.google.inject.Key;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;

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
        for (VcsType type : values()) {
            if (type.toString().equals(name) || type.suffix.equals(name)) {
                return Optional.of(type);
            }
        }
        return Optional.empty();
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
