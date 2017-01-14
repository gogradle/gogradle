package com.github.blindpirate.gogradle.core.pack;

import com.github.blindpirate.gogradle.core.GolangPackage;
import com.github.blindpirate.gogradle.core.VcsGolangPackage;
import com.github.blindpirate.gogradle.core.cache.GlobalCacheManager;
import com.github.blindpirate.gogradle.util.logging.DebugLog;
import com.github.blindpirate.gogradle.vcs.VcsType;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;

import static java.nio.file.Files.exists;

@Singleton
public class GlobalCachePackagePathResolver implements PackagePathResolver {
    private final GlobalCacheManager globalCacheManager;

    @Inject
    public GlobalCachePackagePathResolver(GlobalCacheManager globalCacheManager) {
        this.globalCacheManager = globalCacheManager;
    }

    @Override
    @DebugLog
    public Optional<GolangPackage> produce(String packagePath) {
        Path path = Paths.get(packagePath);
        while (isNotRoot(path)) {
            Optional<VcsType> vcs = findVcsRepo(path);
            if (vcs.isPresent()) {
                return Optional.of(buildPackageInfo(vcs.get(), packagePath, path));
            }
            path = path.getParent();
        }
        return Optional.empty();
    }

    private GolangPackage buildPackageInfo(VcsType vcsType, String packagePath, Path repoRootPath) {
        Path realPath = globalCacheManager.getGlobalPackageCachePath(repoRootPath.toString());
        String url = vcsType.getAccessor().getRemoteUrl(realPath.toFile());
        return VcsGolangPackage.builder()
                .withPath(packagePath)
                .withRootPath(repoRootPath.toString())
                .withVcsType(vcsType)
                .withUrl(url)
                .build();

    }

    private Optional<VcsType> findVcsRepo(Path path) {
        Path realPath = globalCacheManager.getGlobalPackageCachePath(path.toString());
        return Arrays.stream(VcsType.values())
                .filter(vcs -> exists(realPath.resolve(vcs.getRepo())))
                .findFirst();
    }

    private boolean isNotRoot(Path path) {
        return path != null && path.getParent() != null;
    }
}
