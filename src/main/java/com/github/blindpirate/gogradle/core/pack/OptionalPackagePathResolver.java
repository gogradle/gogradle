package com.github.blindpirate.gogradle.core.pack;

import com.github.blindpirate.gogradle.core.GolangPackage;
import com.github.blindpirate.gogradle.util.FactoryUtil;
import com.github.blindpirate.gogradle.util.logging.DebugLog;
import com.google.inject.BindingAnnotation;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Singleton
public class OptionalPackagePathResolver implements PackagePathResolver {
    private Map<String, GolangPackage> cache = new ConcurrentHashMap<>();

    private final List<PackagePathResolver> delegates;

    @Inject
    public OptionalPackagePathResolver(@PackagePathResolvers List<PackagePathResolver> delegates) {
        this.delegates = delegates;
    }

    @Override
    @DebugLog
    public Optional<GolangPackage> produce(String packagePath) {
        Optional<GolangPackage> resultFromCache = tryToFetchFromCache(packagePath);
        if (resultFromCache.isPresent()) {
            return resultFromCache;
        }
        Optional<GolangPackage> result = FactoryUtil.produce(delegates, packagePath);
        updateCache(packagePath, result.get());
        return result;
    }

    private void updateCache(String packagePath, GolangPackage golangPackage) {
        cache.put(packagePath, golangPackage);
        if (golangPackage != GolangPackage.INCOMPLETE) {
            cache.put(golangPackage.getRootPath(), golangPackage.cloneWithSameRoot(golangPackage.getRootPath()));
        }
    }

    private Optional<GolangPackage> tryToFetchFromCache(String packagePath) {
        GolangPackage exactMatch = cache.get(packagePath);
        if (exactMatch != null) {
            return Optional.of(exactMatch);
        }

        Path path = Paths.get(packagePath);
        for (int i = 1; i < path.getNameCount(); ++i) {
            Path current = path.subpath(0, i);
            GolangPackage existingPackage = cache.get(current.toString());
            if (isValid(existingPackage)) {
                GolangPackage result = existingPackage.cloneWithSameRoot(packagePath);
                return Optional.of(result);
            }
        }
        return Optional.empty();
    }

    private boolean isValid(GolangPackage existingPackage) {
        return existingPackage != null && existingPackage != GolangPackage.INCOMPLETE;
    }

    @BindingAnnotation
    @Target({FIELD, PARAMETER, METHOD})
    @Retention(RUNTIME)
    public @interface PackagePathResolvers {
    }
}
