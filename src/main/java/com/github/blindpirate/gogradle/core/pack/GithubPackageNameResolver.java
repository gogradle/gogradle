package com.github.blindpirate.gogradle.core.pack;

import com.github.blindpirate.gogradle.util.Assert;
import com.github.blindpirate.gogradle.vcs.VcsType;

import java.nio.file.Path;
import java.nio.file.Paths;

import static java.util.Arrays.asList;

// github.com/user/project -> git@github.com:user/project.git
// github.com/user/project -> https://github.com/user/project.git
public class GithubPackageNameResolver implements PackageNameResolver {

    private static final String GITHUB_HOST = "github.com/";

    @Override
    public PackageInfo produce(String packageName) {
        Path path = toPath(packageName);
        String httpsUrl = HTTPS + path.subpath(0, 3) + ".git";
        String sshUrl = String.format("git@github.com:%s", path.subpath(1, 3));
        String rootPackageName = path.subpath(0, 3).toString();

        return PackageInfo.builder()
                .withName(packageName)
                .withVcsType(VcsType.Git)
                .withRootName(rootPackageName)
                .withUrls(asList(httpsUrl, sshUrl))
                .build();
    }

    private Path toPath(String packageName) {
        Path path = Paths.get(packageName);
        Assert.isTrue(path.getNameCount() >= 3,
                "Illegal package on github: " + packageName);
        return path;
    }

    @Override
    public boolean accept(String s) {
        return s != null && s.startsWith(GITHUB_HOST);
    }
}
