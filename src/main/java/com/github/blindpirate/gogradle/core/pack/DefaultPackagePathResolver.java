package com.github.blindpirate.gogradle.core.pack;

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
public class DefaultPackagePathResolver implements PackagePathResolver {

    private Map<String, PackageInfo> cache = new ConcurrentHashMap<>();

    private final List<PackagePathResolver> delegates;

    @Inject
    public DefaultPackagePathResolver(@PackagePathResolvers List<PackagePathResolver> delegates) {
        this.delegates = delegates;
    }

    @Override
    @DebugLog
    public Optional<PackageInfo> produce(String packagePath) {
        Optional<PackageInfo> resultFromCache = tryToFetchFromCache(packagePath);
        if (resultFromCache.isPresent()) {
            return resultFromCache;
        }
        Optional<PackageInfo> result = FactoryUtil.produce(delegates, packagePath);
        reportErrorIfResolutionFailed(packagePath, result);
        updateCache(packagePath, result.get());
        return result;
    }

    private void updateCache(String packagePath, PackageInfo packageInfo) {
        cache.put(packagePath, packageInfo);
        if (packageInfo != PackageInfo.INCOMPLETE) {
            cache.put(packageInfo.getRootPath(), packageInfo.cloneWithSameRoot(packageInfo.getRootPath()));
        }
    }

    private Optional<PackageInfo> tryToFetchFromCache(String packagePath) {
        PackageInfo exactMatch = cache.get(packagePath);
        if (exactMatch != null) {
            return Optional.of(exactMatch);
        }

        Path path = Paths.get(packagePath);
        for (int i = 1; i < path.getNameCount(); ++i) {
            Path current = path.subpath(0, i);
            PackageInfo existingPackage = cache.get(current.toString());
            if (isValid(existingPackage)) {
                PackageInfo result = existingPackage.cloneWithSameRoot(packagePath);
                return Optional.of(result);
            }
        }
        return Optional.empty();
    }

    private boolean isValid(PackageInfo existingPackage) {
        return existingPackage != null && existingPackage != PackageInfo.INCOMPLETE;
    }

    private void reportErrorIfResolutionFailed(String packagePath, Optional<PackageInfo> result) {
        if (!result.isPresent()) {
            throw PackageResolutionException.cannotResolvePath(packagePath);
        }
    }

    @BindingAnnotation
    @Target({FIELD, PARAMETER, METHOD})
    @Retention(RUNTIME)
    public @interface PackagePathResolvers {
    }
}
