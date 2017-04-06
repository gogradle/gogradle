package com.github.blindpirate.gogradle.core;

import com.github.blindpirate.gogradle.util.Assert;
import com.github.blindpirate.gogradle.vcs.VcsType;

import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

public class VcsGolangPackage extends ResolvableGolangPackage {
    private VcsInfo originalVcsInfo;
    private VcsInfo substitutedVcsInfo;

    protected VcsGolangPackage(Path rootPath, Path path) {
        super(rootPath, path);
    }

    public VcsType getVcsType() {
        return determineVcs().vcsType;
    }

    public List<String> getUrls() {
        return determineVcs().urls;
    }

    private VcsInfo determineVcs() {
        if (substitutedVcsInfo != null) {
            return substitutedVcsInfo;
        }
        return originalVcsInfo;
    }

    public VcsInfo getOriginalVcsInfo() {
        return originalVcsInfo;
    }

    public VcsInfo getSubstitutedVcsInfo() {
        return substitutedVcsInfo;
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
                .withOriginalVcsInfo(originalVcsInfo)
                .withSubstitutedVcsInfo(substitutedVcsInfo)
                .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Path path;
        private Path rootPath;
        private VcsInfo originalVcsInfo;
        private VcsInfo substitutedVcsInfo;

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

        public Builder withOriginalVcsInfo(VcsType vcsType, List<String> urls) {
            this.originalVcsInfo = new VcsInfo(vcsType, urls);
            return this;
        }

        public Builder withSubstitutedVcsInfo(VcsType vcsType, List<String> urls) {
            this.substitutedVcsInfo = new VcsInfo(vcsType, urls);
            return this;
        }

        public Builder withOriginalVcsInfo(VcsInfo originalVcsInfo) {
            this.originalVcsInfo = originalVcsInfo;
            return this;
        }

        public Builder withSubstitutedVcsInfo(VcsInfo substitutedVcsInfo) {
            this.substitutedVcsInfo = substitutedVcsInfo;
            return this;
        }


        public VcsGolangPackage build() {
            VcsGolangPackage ret = new VcsGolangPackage(rootPath, path);

            ret.originalVcsInfo = originalVcsInfo;
            ret.substitutedVcsInfo = substitutedVcsInfo;

            Assert.isTrue(originalVcsInfo != null || substitutedVcsInfo != null);

            return ret;
        }
    }

    @Override
    public String toString() {
        return "VcsGolangPackage{"
                + "path='" + getPathString() + '\''
                + ", rootPath='" + getRootPathString() + '\''
                + ", vcsType=" + getVcsType()
                + ", urls='" + getUrls() + '\''
                + '}';
    }

    public static class VcsInfo implements Serializable {
        private VcsType vcsType = VcsType.GIT;
        private List<String> urls;

        private VcsInfo(VcsType vcsType, List<String> urls) {
            this.vcsType = vcsType;
            this.urls = urls;
        }

        public VcsType getVcsType() {
            return vcsType;
        }

        public List<String> getUrls() {
            return urls;
        }
    }
}

