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
public class DefaultPackageNameResolver implements PackageNameResolver {

    private Map<String, PackageInfo> cache = new ConcurrentHashMap<>();

    private final List<PackageNameResolver> delegates;

    @Inject
    public DefaultPackageNameResolver(@PackageNameResolvers List<PackageNameResolver> delegates) {
        this.delegates = delegates;
    }

    @Override
    @DebugLog
    public Optional<PackageInfo> produce(String packageName) {
        Optional<PackageInfo> resultFromCache = tryToFetchFromCache(packageName);
        if (resultFromCache.isPresent()) {
            return resultFromCache;
        }
        Optional<PackageInfo> result = FactoryUtil.produce(delegates, packageName);
        reportErrorIfResolutionFailed(packageName, result);
        updateCache(packageName, result.get());
        return result;
    }

    private void updateCache(String packageName, PackageInfo packageInfo) {
        cache.put(packageName, packageInfo);
        if (packageInfo != PackageInfo.INCOMPLETE) {
            cache.put(packageInfo.getRootName(), copyWithSameRoot(packageInfo, packageInfo.getRootName()));
        }
    }

    private Optional<PackageInfo> tryToFetchFromCache(String packageName) {
        PackageInfo exactMatch = cache.get(packageName);
        if (exactMatch != null) {
            return Optional.of(exactMatch);
        }

        Path path = Paths.get(packageName);
        for (int i = 1; i < path.getNameCount(); ++i) {
            Path current = path.subpath(0, i);
            PackageInfo existingPackage = cache.get(current.toString());
            if (isValid(existingPackage)) {
                PackageInfo result = copyWithSameRoot(existingPackage, packageName);
                return Optional.of(result);
            }
        }
        return Optional.empty();
    }

    private PackageInfo copyWithSameRoot(PackageInfo anotherPackage, String packageName) {
        return PackageInfo.builder()
                .withName(packageName)
                .withRootName(anotherPackage.getRootName())
                .withVcsType(anotherPackage.getVcsType())
                .withUrls(anotherPackage.getUrls())
                .build();
    }

    private boolean isValid(PackageInfo existingPackage) {
        return existingPackage != null && existingPackage != PackageInfo.INCOMPLETE;
    }

    private void reportErrorIfResolutionFailed(String packageName, Optional<PackageInfo> result) {
        if (!result.isPresent()) {
            throw PackageResolutionException.cannotResolveName(packageName);
        }
    }

    @BindingAnnotation
    @Target({FIELD, PARAMETER, METHOD})
    @Retention(RUNTIME)
    public @interface PackageNameResolvers {
    }
}
