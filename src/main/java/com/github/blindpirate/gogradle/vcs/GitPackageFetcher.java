package com.github.blindpirate.gogradle.vcs;

import com.github.blindpirate.gogradle.core.cache.git.GitInteractionException;
import com.github.blindpirate.gogradle.util.Assert;
import com.github.blindpirate.gogradle.util.VcsUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.nio.file.Path;
import java.nio.file.Paths;

public class GitPackageFetcher implements PackageFetcher {
    private static final String HTTPS = "https://";
    private static final String HTTP = "http://";

    // https://golang.org/cmd/go/#hdr-Remote_import_paths
    // When a version control system supports multiple protocols,
    // each is tried in turn when downloading. For example, a Git download tries https://,
    // then git+ssh://.
    @Override
    public void fetch(String packageName, Path location) {
        try {
            fetchViaHttps(packageName, location);
            fetchViaHttp(packageName, location);
        } catch (Throwable e) {
            fetchViaSsh(packageName, location);
        }
    }

    protected void fetchViaSsh(String packageName, Path location) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    protected void fetchViaHttp(String packageName, Path location) {
        String repoUrl = getRepoUrlFromPackageName(packageName);
        fetchWithUrl(HTTP + repoUrl, location);
    }

    protected void fetchViaHttps(String packageName, Path location) {
        String repoUrl = getRepoUrlFromPackageName(packageName);
        fetchWithUrl(HTTPS + repoUrl, location);
    }

    protected void fetchWithUrl(String gitUrl, Path location) {
        try {
            Git.cloneRepository()
                    .setURI(gitUrl)
                    .setDirectory(location.toFile())
                    .call();
        } catch (GitAPIException e) {
            throw new GitInteractionException("Exception in git operation", e);
        }
    }

    protected String getRepoUrlFromPackageName(String packageName) {
        Assert.isTrue(packageName.contains(".git"), "absent 'git' suffix in package name:" + packageName);
        int vcsTypeIndex = VcsUtils.vcsSuffixIndexOf(packageName);
        Path path = Paths.get(packageName);
        return path.subpath(0, vcsTypeIndex).toString();
    }
}
