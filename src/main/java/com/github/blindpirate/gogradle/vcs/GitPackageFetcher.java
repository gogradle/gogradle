package com.github.blindpirate.gogradle.vcs;

import com.github.blindpirate.gogradle.util.Assert;
import com.github.blindpirate.gogradle.util.GitUtils;
import com.github.blindpirate.gogradle.util.VcsUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.file.Path;
import java.nio.file.Paths;

@Singleton
public class GitPackageFetcher implements PackageFetcher {

    @Inject
    private GitUtils gitUtils;


    // https://golang.org/cmd/go/#hdr-Remote_import_paths
    // When a version control system supports multiple protocols,
    // each is tried in turn when downloading. For example, a Git download tries https://,
    // then git+ssh://.
    @Override
    public void fetch(String packageName, Path location) {
        if (packageNameStartsWithProtocol(packageName)) {
            gitUtils.cloneWithUrl(packageName, location);
        } else {
            try {
                fetchViaHttps(packageName, location);
                fetchViaHttp(packageName, location);
            } catch (Throwable e) {
                fetchViaSsh(packageName, location);
            }
        }
    }

    private boolean packageNameStartsWithProtocol(String packageName) {
        return packageName != null
                &&
                (packageName.startsWith(HTTP)
                        || packageName.startsWith(HTTPS)
                        || packageName.startsWith(SSH));

    }

    protected void fetchViaSsh(String packageName, Path location) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    protected void fetchViaHttp(String packageName, Path location) {
        String repoUrl = getRepoUrlFromPackageName(packageName);
        gitUtils.cloneWithUrl(HTTP + repoUrl, location);
    }

    protected void fetchViaHttps(String packageName, Path location) {
        String repoUrl = getRepoUrlFromPackageName(packageName);
        gitUtils.cloneWithUrl(HTTPS + repoUrl, location);
    }


    protected String getRepoUrlFromPackageName(String packageName) {
        Assert.isTrue(packageName.contains(".git"), "absent 'git' suffix in package name:" + packageName);
        int vcsTypeIndex = VcsUtils.vcsSuffixIndexOf(packageName);
        Path path = Paths.get(packageName);
        return path.subpath(0, vcsTypeIndex).toString();
    }
}
