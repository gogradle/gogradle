package com.github.blindpirate.gogradle.vcs;

import com.github.blindpirate.gogradle.core.InjectionHelper;
import com.github.blindpirate.gogradle.core.dependency.parse.NotationConverter;
import com.google.inject.Key;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Optional;

public enum VcsType {
    Git("git", ".git", Git.class),
    Mercurial("hg", ".hg", Mercurial.class),
    Svn("svn", ".svn", Svn.class),
    Bazaar("bzr", ".bzr", Bazaar.class);

    private String name;

    private String repo;
    private Class<? extends Annotation> annoClass;

    VcsType(String name, String repo, Class annoClass) {
        this.name = name;
        this.repo = repo;
        this.annoClass = annoClass;
    }

    public String getName() {
        return name;
    }

    public static Optional<VcsType> of(String name) {
        return Arrays.stream(values())
                .filter(type -> type.toString().equalsIgnoreCase(name)
                        || type.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    public <T> T getService(Class<T> serviceClass) {
        return InjectionHelper.INJECTOR_INSTANCE.getInstance(Key.get(serviceClass, annoClass));
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
