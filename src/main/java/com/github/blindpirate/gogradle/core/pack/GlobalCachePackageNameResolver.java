package com.github.blindpirate.gogradle.core.pack;

import com.github.blindpirate.gogradle.core.cache.CacheManager;
import com.github.blindpirate.gogradle.vcs.VcsType;
import com.google.common.base.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static java.nio.file.Files.exists;

@Singleton
public class GlobalCachePackageNameResolver implements PackageNameResolver {
    private final CacheManager cacheManager;

    @Inject
    public GlobalCachePackageNameResolver(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    public Optional<PackageInfo> produce(String packageName) {
        Path path = Paths.get(packageName);
        while (isNotRoot(path)) {
            Optional<VcsType> vcs = findVcsRepo(path);
            if (vcs.isPresent()) {
                return Optional.of(buildPackageInfo(vcs.get(), packageName, path));
            }
            path = path.getParent();
        }
        return Optional.absent();
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
        for (VcsType vcs : VcsType.values()) {
            if (exists(realPath.resolve(vcs.getRepo()))) {
                return Optional.of(vcs);
            }
        }
        return Optional.absent();
    }

    private boolean isNotRoot(Path path) {
        return path.getParent() != null;
    }
}
