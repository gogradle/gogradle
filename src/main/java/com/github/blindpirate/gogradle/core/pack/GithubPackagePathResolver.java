package com.github.blindpirate.gogradle.core.pack;

import com.github.blindpirate.gogradle.core.GolangPackage;
import com.github.blindpirate.gogradle.util.logging.DebugLog;
import com.github.blindpirate.gogradle.vcs.VcsType;
import java.util.Optional;

import java.nio.file.Path;
import java.nio.file.Paths;

import static java.util.Arrays.asList;

// github.com/user/project -> git@github.com:user/project.git
// github.com/user/project -> https://github.com/user/project.git
public class GithubPackagePathResolver implements PackagePathResolver {

    private static final String GITHUB_HOST = "github.com";

    @Override
    @DebugLog
    public Optional<GolangPackage> produce(String packagePath) {
        if (isNotGithubPackage(packagePath)) {
            return Optional.empty();
        } else if (isIncomplete(packagePath)) {
            return Optional.of(GolangPackage.INCOMPLETE);
        } else {
            return doProduce(packagePath);
        }
    }

    private boolean isIncomplete(String packagePath) {
        return toPath(packagePath).getNameCount() < 3;
    }

    private boolean isNotGithubPackage(String packagePath) {
        return !packagePath.startsWith(GITHUB_HOST);
    }

    public Optional<GolangPackage> doProduce(String packagePath) {
        Path path = toPath(packagePath);
        String httpsUrl = HTTPS + path.subpath(0, 3) + ".git";
        String sshUrl = String.format("git@github.com:%s", path.subpath(1, 3));
        String rootPackagePath = path.subpath(0, 3).toString();

        GolangPackage info = GolangPackage.builder()
                .withPath(packagePath)
                .withVcsType(VcsType.Git)
                .withRootPath(rootPackagePath)
                .withUrls(asList(httpsUrl, sshUrl))
                .build();
        return Optional.of(info);
    }

    private Path toPath(String packagePath) {
        return Paths.get(packagePath);
    }

}
