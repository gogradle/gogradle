package com.github.blindpirate.gogradle.core.pack;

import com.github.blindpirate.gogradle.vcs.VcsType;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class PackageInfo {
    // the package path can be recognized, but it's incomplete, e.g. "github.com/a"
    public static final PackageInfo INCOMPLETE =
            builder().withName("INCOMPLETE").build();

    private String name;
    private VcsType vcsType;
    private List<String> urls;
    // the import path corresponding to the repository root
    private String rootName;
    private boolean standard;

    public String getName() {
        return name;
    }

    public VcsType getVcsType() {
        return vcsType;
    }

    public List<String> getUrls() {
        return urls;
    }

    public String getRootName() {
        return rootName;
    }

    public boolean isStandard() {
        return standard;
    }

    public static PackageInfoBuilder builder() {
        return new PackageInfoBuilder();
    }

    public static PackageInfo standardPackage(String packageName) {
        Path path = Paths.get(packageName);
        return builder()
                .withName(packageName)
                .withRootName(path.getName(0).toString())
                .withStandard(true)
                .build();
    }

    public PackageInfo cloneWithSameRoot(String anotherPackage) {
        return builder().
                withName(anotherPackage)
                .withRootName(rootName)
                .withVcsType(vcsType)
                .withUrls(urls)
                .withStandard(standard)
                .build();
    }

    @Override
    public String toString() {
        return "PackageInfo{"
                + "name='" + name + '\''
                + ", vcsType=" + vcsType
                + ", urls=" + urls
                + ", rootName='" + rootName + '\''
                + ", standard=" + standard
                + '}';
    }

    public static final class PackageInfoBuilder {
        private String name;
        private VcsType vcsType;
        private List<String> urls;
        private String rootName;
        private boolean standard;

        public PackageInfoBuilder withName(String name) {
            this.name = name;
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

        public PackageInfoBuilder withRootName(String rootName) {
            this.rootName = rootName;
            return this;
        }

        public PackageInfoBuilder withStandard(boolean standard) {
            this.standard = standard;
            return this;
        }

        public PackageInfo build() {
            PackageInfo packageInfo = new PackageInfo();
            packageInfo.urls = this.urls;
            packageInfo.rootName = this.rootName;
            packageInfo.name = this.name;
            packageInfo.vcsType = this.vcsType;
            packageInfo.standard = this.standard;
            return packageInfo;
        }
    }
}
