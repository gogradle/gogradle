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

package com.github.blindpirate.gogradle.core;

import com.github.blindpirate.gogradle.vcs.VcsType;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class VcsGolangPackage extends ResolvableGolangPackage {
    private GolangRepository repository;

    protected VcsGolangPackage(Path rootPath, Path path) {
        super(rootPath, path);
    }

    @Override
    protected Optional<GolangPackage> longerPath(Path packagePath) {
        // I am github.com/a/b/c, the param is github.com/a/b/c/d
        return Optional.of(sameRoot(packagePath));
    }

    @Override
    protected Optional<GolangPackage> shorterPath(Path packagePath) {
        // I am github.com/a/b/c, the param is github.com/a or github.com/a/b
        if (packagePath.getNameCount() < getRootPath().getNameCount()) {
            // github.com/a
            return Optional.of(IncompleteGolangPackage.of(packagePath));
        } else {
            // github.com/a/b
            return Optional.of(sameRoot(packagePath));
        }
    }

    private GolangPackage sameRoot(Path packagePath) {
        return builder().withPath(packagePath)
                .withRootPath(getRootPath())
                .withRepository(repository)
                .build();
    }

    public GolangRepository getRepository() {
        return repository;
    }

    public VcsType getVcs() {
        return repository.getVcsType();
    }

    public List<String> getUrls() {
        return repository.getUrls();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Path path;
        private Path rootPath;
        private GolangRepository repository;

        private Builder() {
        }

        public Builder withPath(String path) {
            return withPath(Paths.get(path));
        }

        public Builder withRootPath(String rootPath) {
            return withRootPath(Paths.get(rootPath));
        }

        public Builder withPath(Path path) {
            this.path = path;
            return this;
        }

        public Builder withRootPath(Path rootPath) {
            this.rootPath = rootPath;
            return this;
        }

        public Builder withRepository(GolangRepository repository) {
            this.repository = repository;
            return this;
        }

        public VcsGolangPackage build() {
            VcsGolangPackage ret = new VcsGolangPackage(rootPath, path);
            ret.repository = repository;
            return ret;
        }
    }

    @Override
    public String toString() {
        return "VcsGolangPackage{"
                + "path='" + getPathString() + '\''
                + ", rootPath='" + getRootPathString() + '\''
                + ", repo=" + repository
                + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) {
            return false;
        }
        VcsGolangPackage that = (VcsGolangPackage) o;
        return Objects.equals(repository, that.repository);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), repository);
    }

//    public static class VcsInfo implements Serializable {
//        private VcsType vcsType = VcsType.GIT;
//        private List<String> urls;
//
//        private VcsInfo(VcsType vcsType, List<String> urls) {
//            this.vcsType = vcsType;
//            this.urls = urls;
//        }
//
//        public VcsType getVcsType() {
//            return vcsType;
//        }
//
//        public List<String> getUrls() {
//            return urls;
//        }
//
//        @Override
//        public boolean equals(Object o) {
//            if (this == o) {
//                return true;
//            }
//            if (o == null || getClass() != o.getClass()) {
//                return false;
//            }
//            VcsInfo vcsInfo = (VcsInfo) o;
//            return vcsType == vcsInfo.vcsType
//                    && Objects.equals(urls, vcsInfo.urls);
//        }
//
//        @Override
//        public int hashCode() {
//            return Objects.hash(vcsType, urls);
//        }
//    }
}

