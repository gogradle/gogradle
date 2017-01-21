package com.github.blindpirate.gogradle.core.pack;

import com.github.blindpirate.gogradle.core.GolangPackage;
import com.github.blindpirate.gogradle.core.IncompleteGolangPackage;
import com.github.blindpirate.gogradle.core.VcsGolangPackage;
import com.github.blindpirate.gogradle.util.logging.DebugLog;
import com.github.blindpirate.gogradle.vcs.VcsType;

import javax.inject.Singleton;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

// github.com/user/project -> git@github.com:user/project.git
// github.com/user/project -> https://github.com/user/project.git
@Singleton
public class GithubPackagePathResolver implements PackagePathResolver {

    private static final String GITHUB_HOST = "github.com";

    @Override
    @DebugLog
    public Optional<GolangPackage> produce(String packagePath) {
        if (isNotGithubPackage(packagePath)) {
            return Optional.empty();
        } else if (isIncomplete(packagePath)) {
            return Optional.of(IncompleteGolangPackage.of(packagePath));
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

    private Optional<GolangPackage> doProduce(String packagePath) {
        Path path = toPath(packagePath);
        String sshUrl = String.format("https://%s.git", path.subpath(0, 3));
        String rootPackagePath = path.subpath(0, 3).toString();

        GolangPackage info = VcsGolangPackage.builder()
                .withPath(packagePath)
                .withVcsType(VcsType.GIT)
                .withRootPath(rootPackagePath)
                .withUrl(sshUrl)
                .build();
        return Optional.of(info);
    }

    private Path toPath(String packagePath) {
        return Paths.get(packagePath);
    }

}
