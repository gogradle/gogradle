package com.github.blindpirate.gogradle.core.pack;

import com.github.blindpirate.gogradle.util.logging.DebugLog;
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
    @DebugLog
    public Optional<PackageInfo> produce(String packageName) {
        if (isNotGithubPackage(packageName)) {
            return Optional.absent();
        } else if (isIncomplete(packageName)) {
            return Optional.of(PackageInfo.INCOMPLETE);
        } else {
            return doProduce(packageName);
        }
    }

    private boolean isIncomplete(String packageName) {
        return toPath(packageName).getNameCount() < 3;
    }

    private boolean isNotGithubPackage(String packageName) {
        return !packageName.startsWith(GITHUB_HOST);
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
