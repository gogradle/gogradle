package com.github.blindpirate.gogradle.core.pack;

import com.github.blindpirate.gogradle.util.FactoryUtil;
import com.github.blindpirate.gogradle.util.logging.DebugLog;
import com.google.common.base.Optional;
import com.google.inject.BindingAnnotation;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Map;
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
    public DefaultPackageNameResolver(@PackageNameResolvers
                                              List<PackageNameResolver> delegates) {
        this.delegates = delegates;
    }

    @Override
    @DebugLog
    public Optional<PackageInfo> produce(String packageName) {
        PackageInfo resultInCache = cache.get(packageName);
        if (resultInCache != null) {
            return Optional.of(resultInCache);
        }
        Optional<PackageInfo> result = FactoryUtil.produce(delegates, packageName);

        reportErrorIfResolutionFailed(packageName, result);

        cache.put(packageName, result.get());
        return result;
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
