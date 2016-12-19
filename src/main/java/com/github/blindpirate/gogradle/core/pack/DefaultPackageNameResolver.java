package com.github.blindpirate.gogradle.core.pack;

import com.github.blindpirate.gogradle.core.exceptions.DependencyResolutionException;
import com.github.blindpirate.gogradle.util.FactoryUtil;
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
    public PackageInfo produce(String packageName) {
        PackageInfo resultInCache = cache.get(packageName);
        if (resultInCache != null) {
            return resultInCache;
        }
        Optional<PackageInfo> result = FactoryUtil.produce(delegates, packageName);
        if (!result.isPresent()) {
            throw new DependencyResolutionException("Unable to resolve package:" + packageName);
        }
        cache.put(packageName, result.get());
        return result.get();
    }

    @Override
    public boolean accept(String s) {
        return true;
    }

    @BindingAnnotation
    @Target({FIELD, PARAMETER, METHOD})
    @Retention(RUNTIME)
    public @interface PackageNameResolvers {
    }
}
