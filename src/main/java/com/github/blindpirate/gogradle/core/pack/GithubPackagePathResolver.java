package com.github.blindpirate.gogradle.core.pack;

import com.github.blindpirate.gogradle.core.GolangPackage;
import com.github.blindpirate.gogradle.core.VcsGolangPackage;
import com.github.blindpirate.gogradle.vcs.VcsType;

import javax.inject.Singleton;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;

import static com.github.blindpirate.gogradle.util.StringUtils.toUnixString;

// github.com/user/project -> git@github.com:user/project.git
// github.com/user/project -> https://github.com/user/project.git
@Singleton
public class GithubPackagePathResolver extends AbstractPackagePathResolver {

    private static final String GITHUB_HOST = "github.com";


    protected boolean isIncomplete(String packagePath) {
        return Paths.get(packagePath).getNameCount() < 3;
    }

    protected boolean cannotRecognize(String packagePath) {
        return !GITHUB_HOST.equals(Paths.get(packagePath).getName(0).toString());
    }

    protected Optional<GolangPackage> doProduce(String packagePath) {
        Path path = Paths.get(packagePath);
        String sshUrl = String.format("git@%s.git", toUnixString(path.subpath(0, 3)).replaceFirst("/", ":"));
        String httpsUrl = String.format("https://%s.git", toUnixString(path.subpath(0, 3)));

        GolangPackage info = VcsGolangPackage.builder()
                .withPath(path)
                .withOriginalVcsInfo(VcsType.GIT, Arrays.asList(httpsUrl, sshUrl))
                .withRootPath(path.subpath(0, 3))
                .build();
        return Optional.of(info);
    }
}
