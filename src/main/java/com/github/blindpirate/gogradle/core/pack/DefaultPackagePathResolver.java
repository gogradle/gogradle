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

import static com.github.blindpirate.gogradle.util.StringUtils.toUnixString;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Singleton
public class DefaultPackagePathResolver implements PackagePathResolver {
    private Map<String, GolangPackage> cache = new ConcurrentHashMap<>();

    private final List<PackagePathResolver> delegates;

    @Inject
    public DefaultPackagePathResolver(@PackagePathResolvers List<PackagePathResolver> delegates) {
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
        Path path = Paths.get(packagePath);
        for (int i = path.getNameCount(); i > 0; --i) {
            Path current = path.subpath(0, i);
            String currentPathStr = toUnixString(current);
            cache.put(currentPathStr, golangPackage.resolve(currentPathStr).get());
        }
    }

    private Optional<GolangPackage> tryToFetchFromCache(String packagePath) {
        Path path = Paths.get(packagePath);
        for (int i = path.getNameCount(); i > 0; --i) {
            Path current = path.subpath(0, i);
            GolangPackage existentPackage = cache.get(toUnixString(current));

            if (existentPackage != null) {
                return existentPackage.resolve(packagePath);
            }
        }
        return Optional.empty();
    }

    @BindingAnnotation
    @Target({FIELD, PARAMETER, METHOD})
    @Retention(RUNTIME)
    public @interface PackagePathResolvers {
    }
}
