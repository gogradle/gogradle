package com.github.blindpirate.gogradle.core.pack;

import com.github.blindpirate.gogradle.core.GolangPackage;
import com.github.blindpirate.gogradle.core.VcsGolangPackage;
import com.github.blindpirate.gogradle.core.cache.GlobalCacheManager;
import com.github.blindpirate.gogradle.core.cache.GlobalCacheMetadata;
import com.github.blindpirate.gogradle.util.logging.DebugLog;
import com.github.blindpirate.gogradle.vcs.VcsType;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

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
        for (int i = path.getNameCount(); i > 0; i--) {
            Path subpath = path.subpath(0, i);
            Optional<GlobalCacheMetadata> metadata = globalCacheManager.getMetadata(subpath);
            if (metadata.isPresent() && !metadata.get().isTemp()) {
                VcsGolangPackage pkg = VcsGolangPackage.builder()
                        .withPath(packagePath)
                        .withRootPath(subpath)
                        .withVcsType(VcsType.of(metadata.get().getVcs()).get())
                        .withUrls(metadata.get().getOriginalUrls())
                        .build();
                return Optional.of(pkg);
            }
        }

        return Optional.empty();
    }
}
