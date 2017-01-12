package com.github.blindpirate.gogradle.core;

import com.github.blindpirate.gogradle.vcs.VcsType;

import java.util.Optional;

public class VcsGolangPackage extends GolangPackage {
    private String rootPath;
    private VcsType vcsType;
    private String url;

    private VcsGolangPackage(String path) {
        super(path);
    }

    @Override
    public String getRootPath() {
        return rootPath;
    }

    @Override
    public VcsType getVcsType() {
        return vcsType;
    }


    @Override
    public String getUrl() {
        return url;
    }

    @Override
    protected Optional<GolangPackage> longerPath(String packagePath) {
        // I am github.com/a/b/c, the param is github.com/a/b/c/d
        return Optional.of(sameRoot(packagePath));
    }

    @Override
    protected Optional<GolangPackage> shorterPath(String packagePath) {
        // I am github.com/a/b/c, the param is github.com/a or github.com/a/b
        if (packagePath.length() < rootPath.length()) {
            // github.com/a
            return Optional.of(IncompleteGolangPackage.of(packagePath));
        } else {
            // github.com/a/b
            return Optional.of(sameRoot(packagePath));
        }
    }

    private GolangPackage sameRoot(String packagePath) {
        return builder().withPath(packagePath)
                .withRootPath(rootPath)
                .withVcsType(vcsType)
                .withUrl(url)
                .build();
    }


    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String path;
        private String rootPath;
        private VcsType vcsType;
        private String url;

        private Builder() {
        }

        public Builder withPath(String path) {
            this.path = path;
            return this;
        }

        public Builder withRootPath(String rootPath) {
            this.rootPath = rootPath;
            return this;
        }

        public Builder withVcsType(VcsType vcsType) {
            this.vcsType = vcsType;
            return this;
        }

        public Builder withUrl(String url) {
            this.url = url;
            return this;
        }

        public VcsGolangPackage build() {
            VcsGolangPackage vcsGolangPackage = new VcsGolangPackage(path);
            vcsGolangPackage.rootPath = this.rootPath;
            vcsGolangPackage.url = this.url;
            vcsGolangPackage.vcsType = this.vcsType;
            return vcsGolangPackage;
        }
    }

    @Override
    public String toString() {
        return "VcsGolangPackage{"
                + "path='" + getPath() + '\''
                + "rootPath='" + rootPath + '\''
                + ", vcsType=" + vcsType
                + ", url='" + url + '\''
                + '}';
    }
}
