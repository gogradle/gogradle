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
import java.util.List;
import java.util.Optional;

import static com.github.blindpirate.gogradle.vcs.VcsScheme.BZR;
import static com.github.blindpirate.gogradle.vcs.VcsScheme.BZR_SSH;
import static com.github.blindpirate.gogradle.vcs.VcsScheme.GIT_SSH;
import static com.github.blindpirate.gogradle.vcs.VcsScheme.HTTP;
import static com.github.blindpirate.gogradle.vcs.VcsScheme.HTTPS;
import static com.github.blindpirate.gogradle.vcs.VcsScheme.SSH;
import static com.github.blindpirate.gogradle.vcs.VcsScheme.SVN_SSH;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;

// https://github.com/golang/go/blob/1102616c772c262175f5ba5f12d6b574d0ad9101/src/cmd/go/internal/get/vcs.go
public enum VcsType {
    GIT("git", Git.class, asList(VcsScheme.GIT, HTTPS, HTTP, GIT_SSH, SSH)),
    MERCURIAL("hg", Mercurial.class, asList(HTTPS, HTTP, SSH)),
    SVN("svn", Svn.class, asList(HTTPS, HTTP, VcsScheme.SVN, SVN_SSH)),
    BAZAAR("bzr", Bazaar.class, asList(HTTPS, HTTP, BZR, BZR_SSH));

    private String name;

    private String suffix;

    private Class<? extends Annotation> annoClass;

    private List<VcsScheme> schemes;

    VcsType(String name, Class<? extends Annotation> annoClass, List<VcsScheme> schemes) {
        this.name = name;
        this.suffix = "." + name;
        this.annoClass = annoClass;
        this.schemes = schemes;
    }

    public String getName() {
        return name;
    }

    public String getSuffix() {
        return suffix;
    }

    public List<VcsScheme> getSchemes() {
        return schemes;
    }

    public static Optional<VcsType> of(String name) {
        return stream(values())
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
