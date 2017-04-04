package com.github.blindpirate.gogradle.core.dependency;

import com.github.blindpirate.gogradle.GogradleGlobal;
import com.github.blindpirate.gogradle.util.Assert;
import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.DomainObjectCollection;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.TaskDependency;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Function;

public class GolangDependencySet implements Set<GolangDependency>, Serializable {

    private TreeSet<GolangDependency> container = new TreeSet<>(
            Comparator.comparing((Serializable & Function<GolangDependency, String>) GolangDependency::getName));

    public GolangDependencySet(Collection<? extends GolangDependency> dependencies) {
        container.addAll(dependencies);
    }

    public GolangDependencySet() {
    }

    public static GolangDependencySet merge(GolangDependencySet... sets) {
        GolangDependencySet result = new GolangDependencySet();
        for (GolangDependencySet set : sets) {
            for (GolangDependency dependency : set) {
                if (!result.contains(dependency)) {
                    result.add(dependency);
                }
            }
        }
        return result;
    }

    public List<GolangDependency> flatten() {
        List<GolangDependency> result = new ArrayList<>();
        this.forEach((Serializable & Consumer<GolangDependency>)
                dependency -> dfs(dependency, result, 0));
        return result;
    }

    private void dfs(GolangDependency dependency, List<GolangDependency> result, int depth) {
        Assert.isTrue(depth < GogradleGlobal.MAX_DFS_DEPTH);
        result.add(dependency);

        if (dependency instanceof ResolvedDependency) {
            ResolvedDependency.class.cast(dependency)
                    .getDependencies()
                    .forEach((Serializable & Consumer<GolangDependency>)
                            subDependency -> dfs(subDependency, result, depth + 1));
        }
    }

    public DependencySet toDependencySet() {
        return new DependencySetFacade(this);
    }

    public static GolangDependencySet empty() {
        return new GolangDependencySet();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GolangDependencySet that = (GolangDependencySet) o;
        return Objects.equals(container, that.container);
    }

    @Override
    public int hashCode() {
        return Objects.hash(container);
    }

    @Override
    public int size() {
        return container.size();
    }

    @Override
    public boolean isEmpty() {
        return container.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return container.contains(o);
    }

    @Override
    public Iterator<GolangDependency> iterator() {
        return container.iterator();
    }

    @Override
    public Object[] toArray() {
        return container.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return container.toArray(a);
    }

    @Override
    public boolean add(GolangDependency dependency) {
        return container.add(dependency);
    }

    @Override
    public boolean remove(Object o) {
        return container.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return container.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends GolangDependency> c) {
        return container.addAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return container.retainAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return container.removeAll(c);
    }

    @Override
    public void clear() {
        container.clear();
    }

    @SuppressWarnings("unchecked")
    public static class DependencySetFacade implements DependencySet, Serializable {

        private final GolangDependencySet outerInstance;

        private DependencySetFacade(GolangDependencySet outerInstance) {
            this.outerInstance = outerInstance;
        }

        public GolangDependencySet toGolangDependencies() {
            return outerInstance;
        }

        @Override
        public <S extends Dependency> DomainObjectSet<S> withType(Class<S> type) {
            throw new UnsupportedOperationException("Unsupported method withType is invoked!");
        }

        @Override
        public <S extends Dependency> DomainObjectCollection<S> withType(Class<S> type,
                                                                         Action<? super S> configureAction) {
            throw new UnsupportedOperationException("Unsupported method withType is invoked!");
        }

        @Override
        public <S extends Dependency> DomainObjectCollection<S> withType(Class<S> type, Closure configureClosure) {
            throw new UnsupportedOperationException("Unsupported method withType is invoked!");
        }

        @Override
        public DomainObjectSet<Dependency> matching(Spec<? super Dependency> spec) {
            throw new UnsupportedOperationException("Unsupported method matching is invoked!");
        }

        @Override
        public DomainObjectSet<Dependency> matching(Closure spec) {
            throw new UnsupportedOperationException("Unsupported method matching is invoked!");
        }

        @Override
        public Action<? super Dependency> whenObjectAdded(Action<? super Dependency> action) {
            throw new UnsupportedOperationException("Unsupported method whenObjectAdded is invoked!");
        }

        @Override
        public void whenObjectAdded(Closure action) {
            throw new UnsupportedOperationException("Unsupported method whenObjectAdded is invoked!");
        }

        @Override
        public Action<? super Dependency> whenObjectRemoved(Action<? super Dependency> action) {
            throw new UnsupportedOperationException("Unsupported method whenObjectRemoved is invoked!");
        }

        @Override
        public void whenObjectRemoved(Closure action) {
            throw new UnsupportedOperationException("Unsupported method whenObjectRemoved is invoked!");
        }

        @Override
        public void all(Action<? super Dependency> action) {
            throw new UnsupportedOperationException("Unsupported method all is invoked!");
        }

        @Override
        public void all(Closure action) {
            throw new UnsupportedOperationException("Unsupported method all is invoked!");
        }

        @Override
        public Set<Dependency> findAll(Closure spec) {
            throw new UnsupportedOperationException("Unsupported method findAll is invoked!");
        }

        @Override
        public TaskDependency getBuildDependencies() {
            throw new UnsupportedOperationException("Unsupported method getBuildDependencies is invoked!");
        }

        @Override
        public int size() {
            return outerInstance.size();
        }

        @Override
        public boolean isEmpty() {
            return outerInstance.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            return outerInstance.contains(o);
        }

        @Override
        public Iterator<Dependency> iterator() {
            return (Iterator) outerInstance.iterator();
        }

        @Override
        public Object[] toArray() {
            return outerInstance.toArray();
        }

        @Override
        public <T> T[] toArray(T[] a) {
            return outerInstance.toArray(a);
        }

        @Override
        public boolean add(Dependency dependency) {
            return outerInstance.add((GolangDependency) dependency);
        }

        @Override
        public boolean remove(Object o) {
            return outerInstance.remove(o);
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return outerInstance.containsAll(c);
        }

        @Override
        public boolean addAll(Collection<? extends Dependency> c) {
            return outerInstance.addAll((Collection) c);
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            return outerInstance.removeAll(c);
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            return outerInstance.retainAll(c);
        }

        @Override
        public void clear() {
            outerInstance.clear();
        }
    }

}
