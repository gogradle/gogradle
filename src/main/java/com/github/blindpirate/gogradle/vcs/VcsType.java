/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.github.blindpirate.gogradle.vcs;

import com.github.blindpirate.gogradle.GogradleGlobal;
import com.github.blindpirate.gogradle.core.dependency.parse.NotationConverter;
import com.google.inject.Key;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Optional;

public enum VcsType {
    GIT("git", Git.class),
    MERCURIAL("hg", Mercurial.class),
    SVN("svn", Svn.class),
    BAZAAR("bzr", Bazaar.class);

    private String name;

    private Class<? extends Annotation> annoClass;

    VcsType(String name, Class annoClass) {
        this.name = name;
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
        return GogradleGlobal.getInstance(Key.get(serviceClass, annoClass));
    }

    public VcsAccessor getAccessor() {
        return getService(VcsAccessor.class);
    }

    public NotationConverter getNotationConverter() {
        return getService(NotationConverter.class);
    }
}
