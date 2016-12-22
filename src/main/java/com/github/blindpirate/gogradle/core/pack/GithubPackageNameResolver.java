package com.github.blindpirate.gogradle.core.pack;

import com.github.blindpirate.gogradle.util.Assert;
import com.github.blindpirate.gogradle.vcs.VcsType;
import com.google.common.base.Optional;

import java.nio.file.Path;
import java.nio.file.Paths;

import static java.util.Arrays.asList;

// github.com/user/project -> git@github.com:user/project.git
// github.com/user/project -> https://github.com/user/project.git
public class GithubPackageNameResolver implements PackageNameResolver {

    private static final String GITHUB_HOST = "github.com";

    @Override
    public Optional<PackageInfo> produce(String packageName) {

        if (isInvalidGithubPackage(packageName)) {
            return Optional.absent();
        } else {
            return doProduce(packageName);
        }
    }

    private boolean isInvalidGithubPackage(String packageName) {
        return !packageName.startsWith(GITHUB_HOST)
                || toPath(packageName).getNameCount() < 3;
    }

    public Optional<PackageInfo> doProduce(String packageName) {
        Path path = toPath(packageName);
        String httpsUrl = HTTPS + path.subpath(0, 3) + ".git";
        String sshUrl = String.format("git@github.com:%s", path.subpath(1, 3));
        String rootPackageName = path.subpath(0, 3).toString();

        PackageInfo info = PackageInfo.builder()
                .withName(packageName)
                .withVcsType(VcsType.Git)
                .withRootName(rootPackageName)
                .withUrls(asList(httpsUrl, sshUrl))
                .build();
        return Optional.of(info);
    }

    private Path toPath(String packageName) {
        return Paths.get(packageName);
    }

}
