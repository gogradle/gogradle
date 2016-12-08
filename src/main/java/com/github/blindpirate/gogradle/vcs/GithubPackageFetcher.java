package com.github.blindpirate.gogradle.vcs;

import com.github.blindpirate.gogradle.util.Assert;

import java.nio.file.Path;
import java.nio.file.Paths;

public class GithubPackageFetcher extends GitPackageFetcher {

    //import "github.com/user/project"
    //import "github.com/user/project/sub/directory"
    @Override
    protected void fetchViaHttp(String packageName, Path location) {
        throw new UnsupportedOperationException("Github does not support http!");
    }

    @Override
    protected void fetchViaSsh(String packageName, Path location) {
        // github.com/user/project -> git@github.com:user/project
        Path path = Paths.get(packageName);
        Assert.isTrue(path.getNameCount() >= 3,
                "Illegal package on github:" + packageName);
        String sshUrl = String.format("git@github.com:%s", path.subpath(1, 3));
        fetchWithUrl(sshUrl, location);
    }

    @Override
    protected String getRepoUrlFromPackageName(String packageName) {
        Path path = Paths.get(packageName);
        Assert.isTrue(path.getNameCount() >= 3,
                "Illegal package on github:" + packageName);
        return path.subpath(0, 3) + ".git";
    }
}
