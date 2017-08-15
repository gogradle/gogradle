/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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
    // if a VcsGolangPackage is cached, its urls must be original instead of converted
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

    public void updateCache(String packagePath, GolangPackage golangPackage) {
        // if github.com/a/b/c is resolved, then all its subpath are resolved:
        // github.com/a/b
        // github.com/a
        // github.com
        Path path = Paths.get(packagePath);
        for (int i = path.getNameCount(); i > 0; --i) {
            Path current = path.subpath(0, i);
            String currentPathStr = toUnixString(current);
            cache.put(currentPathStr, golangPackage.resolve(current).get());
        }
    }

    private Optional<GolangPackage> tryToFetchFromCache(String packagePath) {
        // when github.com/a/b/c not found, fetch its subpath in order:
        // github.com/a/b
        // github.com/a
        // github.com
        Path path = Paths.get(packagePath);
        for (int i = path.getNameCount(); i > 0; --i) {
            Path current = path.subpath(0, i);
            GolangPackage existentPackage = cache.get(toUnixString(current));

            if (existentPackage != null) {
                return existentPackage.resolve(path);
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
