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

import com.github.blindpirate.gogradle.core.dependency.parse.MapNotationParser;
import com.github.blindpirate.gogradle.util.Assert;
import com.github.blindpirate.gogradle.util.ConfigureUtils;
import com.github.blindpirate.gogradle.util.MapUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

import static com.github.blindpirate.gogradle.core.dependency.AbstractNotationDependency.NoTransitivePredicate.NO_TRANSITIVE_PREDICATE;

public abstract class AbstractNotationDependency extends AbstractGolangDependency implements NotationDependency {
    public static final Predicate<GolangDependency> NO_TRANSITIVE_DEP_PREDICATE = NO_TRANSITIVE_PREDICATE;

    public static final String VERSION_KEY = "version";

    private ResolvedDependency resolvedDependency;

    /**
     * The {@link GolangDependency} matching any of this set will be excluded from transitive dependencies.
     */
    protected Set<Predicate<GolangDependency>> transitiveDepExclusions = new HashSet<>();

    @Override
    public Set<Predicate<GolangDependency>> getTransitiveDepExclusions() {
        return new HashSet<>(transitiveDepExclusions);
    }

    @Override
    public ResolvedDependency resolve(ResolveContext context) {
        if (resolvedDependency == null) {
            resolvedDependency = doResolve(context);
        }
        return resolvedDependency;
    }

    public boolean hasBeenResolved() {
        return resolvedDependency != null;
    }

    protected abstract ResolvedDependency doResolve(ResolveContext context);

    public void exclude(Map<String, Object> map) {
        transitiveDepExclusions.add(PropertiesExclusionPredicate.of(map));
    }

    public void setTransitive(boolean transitive) {
        if (transitive) {
            transitiveDepExclusions.remove(NO_TRANSITIVE_DEP_PREDICATE);
        } else {
            transitiveDepExclusions.add(NO_TRANSITIVE_DEP_PREDICATE);
        }
    }

    @Override
    public Object clone() {
        AbstractNotationDependency ret = (AbstractNotationDependency) super.clone();
        ret.transitiveDepExclusions = this.getTransitiveDepExclusions();
        ret.resolvedDependency = null;
        return ret;
    }

    public enum NoTransitivePredicate implements Predicate<GolangDependency> {
        NO_TRANSITIVE_PREDICATE;

        @Override
        public boolean test(GolangDependency dependency) {
            return true;
        }
    }

    public static class PropertiesExclusionPredicate implements Predicate<GolangDependency>, Serializable {
        private Map<String, Object> properties;

        public static PropertiesExclusionPredicate of(Map<String, Object> properties) {
            PropertiesExclusionPredicate ret = new PropertiesExclusionPredicate();
            ret.properties = Assert.isNotNull(properties);
            return ret;
        }

        @Override
        public boolean test(GolangDependency dependency) {
            Map<String, Object> tmp = new HashMap<>(properties);
            String name = MapUtils.getString(tmp, MapNotationParser.NAME_KEY);
            tmp.remove(MapNotationParser.NAME_KEY);
            if (name != null) {
                return dependency.getName().startsWith(name) && ConfigureUtils.match(tmp, dependency);
            } else {
                return ConfigureUtils.match(tmp, dependency);
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            PropertiesExclusionPredicate that = (PropertiesExclusionPredicate) o;

            return properties.equals(that.properties);
        }

        @Override
        public int hashCode() {
            return properties.hashCode();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) {
            return false;
        }
        AbstractNotationDependency that = (AbstractNotationDependency) o;
        return Objects.equals(transitiveDepExclusions, that.transitiveDepExclusions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transitiveDepExclusions, super.hashCode());
    }
}
