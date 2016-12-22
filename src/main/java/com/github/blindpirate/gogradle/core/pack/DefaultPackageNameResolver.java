package com.github.blindpirate.gogradle.core.pack;

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

    private Map<String, Optional<PackageInfo>> cache = new ConcurrentHashMap<>();

    private final List<PackageNameResolver> delegates;

    @Inject
    public DefaultPackageNameResolver(@PackageNameResolvers
                                              List<PackageNameResolver> delegates) {
        this.delegates = delegates;
    }

    @Override
    public Optional<PackageInfo> produce(String packageName) {
        Optional<PackageInfo> resultInCache = cache.get(packageName);
        if (resultInCache != null) {
            return resultInCache;
        }
        Optional<PackageInfo> result = FactoryUtil.produce(delegates, packageName);
        cache.put(packageName, result);
        return result;
    }

    @BindingAnnotation
    @Target({FIELD, PARAMETER, METHOD})
    @Retention(RUNTIME)
    public @interface PackageNameResolvers {
    }
}
