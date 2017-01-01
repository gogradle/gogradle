package com.github.blindpirate.gogradle.core;

import com.github.blindpirate.gogradle.vcs.VcsType;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class GolangPackage {
    // the package path can be recognized, but it's incomplete, e.g. "github.com/a"
    public static final GolangPackage INCOMPLETE =
            builder().withPath("INCOMPLETE").build();

    private String path;
    private VcsType vcsType;
    private List<String> urls;
    // the import path corresponding to the repository root
    private String rootPath;
    private boolean standard;

    public String getPath() {
        return path;
    }

    public VcsType getVcsType() {
        return vcsType;
    }

    public List<String> getUrls() {
        return urls;
    }

    public String getRootPath() {
        return rootPath;
    }

    public boolean isStandard() {
        return standard;
    }

    public static PackageInfoBuilder builder() {
        return new PackageInfoBuilder();
    }

    public static GolangPackage standardPackage(String packagePath) {
        Path path = Paths.get(packagePath);
        return builder()
                .withPath(packagePath)
                .withRootPath(path.getName(0).toString())
                .withStandard(true)
                .build();
    }

    public GolangPackage cloneWithSameRoot(String anotherPackage) {
        return builder().
                withPath(anotherPackage)
                .withRootPath(rootPath)
                .withVcsType(vcsType)
                .withUrls(urls)
                .withStandard(standard)
                .build();
    }

    @Override
    public String toString() {
        return "GolangPackage{"
                + "path='" + path + '\''
                + ", vcsType=" + vcsType
                + ", urls=" + urls
                + ", rootPath='" + rootPath + '\''
                + ", standard=" + standard
                + '}';
    }

    public static final class PackageInfoBuilder {
        private String path;
        private VcsType vcsType;
        private List<String> urls;
        private String rootPath;
        private boolean standard;

        public PackageInfoBuilder withPath(String path) {
            this.path = path;
            return this;
        }

        public PackageInfoBuilder withVcsType(VcsType vcsType) {
            this.vcsType = vcsType;
            return this;
        }

        public PackageInfoBuilder withUrls(List<String> urls) {
            this.urls = urls;
            return this;
        }

        public PackageInfoBuilder withUrl(String url) {
            this.urls = Arrays.asList(url);
            return this;
        }

        public PackageInfoBuilder withRootPath(String rootPath) {
            this.rootPath = rootPath;
            return this;
        }

        public PackageInfoBuilder withStandard(boolean standard) {
            this.standard = standard;
            return this;
        }

        public GolangPackage build() {
            GolangPackage golangPackage = new GolangPackage();
            golangPackage.urls = this.urls;
            golangPackage.rootPath = this.rootPath;
            golangPackage.path = this.path;
            golangPackage.vcsType = this.vcsType;
            golangPackage.standard = this.standard;
            return golangPackage;
        }
    }
}
