package com.github.blindpirate.gogradle.core;

import com.github.blindpirate.gogradle.vcs.VcsType;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.github.blindpirate.gogradle.util.CollectionUtils.isEmpty;

public class VcsGolangPackage extends GolangPackage {
    private String rootPath;
    private VcsType vcsType;
    private List<String> urls;

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


    public String getUrl() {
        return isEmpty(urls) ? null : urls.get(0);
    }

    @Override
    public List<String> getUrls() {
        return urls;
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
                .withUrls(urls)
                .build();
    }


    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String path;
        private String rootPath;
        private VcsType vcsType;
        private List<String> urls;

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
            this.urls = Arrays.asList(url);
            return this;
        }

        public Builder withUrls(List<String> urls) {
            this.urls = urls;
            return this;
        }

        public VcsGolangPackage build() {
            VcsGolangPackage vcsGolangPackage = new VcsGolangPackage(path);
            vcsGolangPackage.rootPath = this.rootPath;
            vcsGolangPackage.urls = this.urls;
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
                + ", url='" + urls + '\''
                + '}';
    }
}
