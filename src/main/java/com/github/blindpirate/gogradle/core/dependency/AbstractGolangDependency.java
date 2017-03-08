package com.github.blindpirate.gogradle.core.dependency;

import com.github.blindpirate.gogradle.util.Assert;
import com.github.blindpirate.gogradle.util.ConfigureUtils;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.specs.Spec;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class AbstractGolangDependency implements GolangDependency {
    public static final Spec<GolangDependency> NO_TRANSITIVE_DEP_SPEC = NoTransitiveSpec.NO_TRANSITIVE_SPEC;
    private String name;
    private boolean firstLevel;
    /**
     * The {@link GolangDependency} matching any of this set will be excluded from transitive dependencies.
     */
    protected Set<Spec<GolangDependency>> transitiveDepExclusions = new HashSet<>();

    protected boolean shouldNotBeExcluded(GolangDependency dependency) {
        return transitiveDepExclusions.stream().noneMatch(spec -> spec.isSatisfiedBy(dependency));
    }

    @Override
    public boolean isFirstLevel() {
        return firstLevel;
    }

    public void setFirstLevel(boolean firstLevel) {
        this.firstLevel = firstLevel;
    }

    protected void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getGroup() {
        throw new UnsupportedOperationException("Unsupported method getGroup is invoked!");
    }

    @Override
    public String getVersion() {
        throw new UnsupportedOperationException("Unsupported method getVersion is invoked!");
    }

    @Override
    public boolean contentEquals(Dependency dependency) {
        throw new UnsupportedOperationException("Unsupported method contentEquals is invoked!");
    }

    @Override
    public Dependency copy() {
        throw new UnsupportedOperationException("Unsupported method copy is invoked!");
    }

    public enum NoTransitiveSpec implements Spec<GolangDependency> {
        NO_TRANSITIVE_SPEC;

        @Override
        public boolean isSatisfiedBy(GolangDependency dependency) {
            return true;
        }
    }

    public static class PropertiesExclusionSpec implements Spec<GolangDependency> {
        private Map<String, Object> properties;

        public static PropertiesExclusionSpec of(Map<String, Object> properties) {
            PropertiesExclusionSpec ret = new PropertiesExclusionSpec();
            ret.properties = Assert.isNotNull(properties);
            return ret;
        }

        @Override
        public boolean isSatisfiedBy(GolangDependency dependency) {
            return ConfigureUtils.match(properties, dependency);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            PropertiesExclusionSpec that = (PropertiesExclusionSpec) o;

            return properties.equals(that.properties);
        }

        @Override
        public int hashCode() {
            return properties.hashCode();
        }
    }
}
