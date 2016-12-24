package com.github.blindpirate.gogradle.core.pack;

import com.github.blindpirate.gogradle.core.cache.CacheManager;
import com.github.blindpirate.gogradle.util.logging.DebugLog;
import com.github.blindpirate.gogradle.vcs.VcsType;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static java.nio.file.Files.exists;

@Singleton
public class GlobalCachePackageNameResolver implements PackageNameResolver {
    private final CacheManager cacheManager;

    @Inject
    public GlobalCachePackageNameResolver(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    @DebugLog
    public Optional<PackageInfo> produce(String packageName) {
        Path path = Paths.get(packageName);
        while (isNotRoot(path)) {
            Optional<VcsType> vcs = findVcsRepo(path);
            if (vcs.isPresent()) {
                return Optional.of(buildPackageInfo(vcs.get(), packageName, path));
            }
            path = path.getParent();
        }
        return Optional.empty();
    }

    private PackageInfo buildPackageInfo(VcsType vcsType, String packageName, Path repoRootPath) {
        Path realPath = cacheManager.getGlobalCachePath(repoRootPath.toString());
        List<String> urls = vcsType.getAccessor().getRemoteUrls(realPath);
        return PackageInfo.builder()
                .withName(packageName)
                .withRootName(repoRootPath.toString())
                .withVcsType(vcsType)
                .withUrls(urls)
                .build();

    }

    private Optional<VcsType> findVcsRepo(Path path) {
        Path realPath = cacheManager.getGlobalCachePath(path.toString());
        return Arrays.stream(VcsType.values())
                .filter(vcs -> exists(realPath.resolve(vcs.getRepo())))
                .findFirst();
    }

    private boolean isNotRoot(Path path) {
        return path != null && path.getParent() != null;
    }
}
