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

package com.github.blindpirate.gogradle.core.dependency;

import com.github.blindpirate.gogradle.core.UnrecognizedGolangPackage;
import com.github.blindpirate.gogradle.core.cache.CacheScope;
import com.github.blindpirate.gogradle.core.exceptions.UnrecognizedPackageException;

import java.util.Set;
import java.util.function.Predicate;

public class UnrecognizedNotationDependency extends AbstractGolangDependency implements NotationDependency {

    public static UnrecognizedNotationDependency of(UnrecognizedGolangPackage pkg) {
        UnrecognizedNotationDependency ret = new UnrecognizedNotationDependency();
        ret.setPackage(pkg);
        ret.setName(pkg.getPathString());
        return ret;
    }

    private UnrecognizedNotationDependency() {
    }

    @Override
    public boolean isFirstLevel() {
        throw new UnsupportedOperationException();
    }

    @Override
    public CacheScope getCacheScope() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Predicate<GolangDependency>> getTransitiveDepExclusions() {
        throw UnrecognizedPackageException.cannotRecognizePackage((UnrecognizedGolangPackage) getPackage());
    }

    @Override
    public ResolvedDependency resolve(ResolveContext context) {
        throw UnrecognizedPackageException.cannotRecognizePackage((UnrecognizedGolangPackage) getPackage());
    }
}
