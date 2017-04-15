package com.github.blindpirate.gogradle.core.dependency;

import com.github.blindpirate.gogradle.GogradleGlobal;
import com.github.blindpirate.gogradle.core.dependency.parse.MapNotationParser;
import com.github.blindpirate.gogradle.core.dependency.resolve.DependencyResolver;
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

    protected ResolvedDependency doResolve(ResolveContext context) {
        DependencyResolver resolver = GogradleGlobal.getInstance(this.getResolverClass());
        return resolver.resolve(context, this);
    }

    protected abstract Class<? extends DependencyResolver> getResolverClass();

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
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AbstractNotationDependency that = (AbstractNotationDependency) o;
        return Objects.equals(transitiveDepExclusions, that.transitiveDepExclusions)
                && Objects.equals(getName(), that.getName())
                && Objects.equals(isFirstLevel(), that.isFirstLevel());
    }

    @Override
    public int hashCode() {
        return Objects.hash(transitiveDepExclusions, getName(), isFirstLevel());
    }
}
