package com.github.blindpirate.gogradle.core.dependency;

import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.DomainObjectCollection;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.internal.DefaultNamedDomainObjectSet;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.TaskDependency;
import org.gradle.internal.reflect.DirectInstantiator;
import org.gradle.internal.reflect.Instantiator;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

public class GolangDependencySet extends DefaultNamedDomainObjectSet<GolangDependency> {
    public GolangDependencySet(Collection<? extends GolangDependency> dependencies) {
        this();
        dependencies.forEach(d -> this.add(d));
    }

    public GolangDependencySet() {
        this(GolangDependency.class, DirectInstantiator.INSTANCE);
    }

    public GolangDependencySet(Class<? extends GolangDependency> type, Instantiator instantiator) {
        super(type, instantiator, GolangDependency.Namer.INSTANCE);
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

    public DependencySet toDependencySet() {
        return new DependencySetFacade();
    }

    public static GolangDependencySet empty() {
        return new GolangDependencySet();
    }

    public class DependencySetFacade implements DependencySet {

        public GolangDependencySet toGolangDependencies() {
            return GolangDependencySet.this;
        }

        @Override
        public <S extends Dependency> DomainObjectSet<S> withType(Class<S> type) {
            return (DomainObjectSet) GolangDependencySet.this.withType((Class) type);
        }

        @Override
        public <S extends Dependency> DomainObjectCollection<S> withType(Class<S> type,
                                                                         Action<? super S> configureAction) {
            return (DomainObjectSet) GolangDependencySet.this.withType((Class) type, (Action) configureAction);
        }

        @Override
        public <S extends Dependency> DomainObjectCollection<S> withType(Class<S> type, Closure configureClosure) {
            return (DomainObjectSet) GolangDependencySet.this.withType((Class) type, configureClosure);
        }

        @Override
        public DomainObjectSet<Dependency> matching(Spec<? super Dependency> spec) {
            return (DomainObjectSet) GolangDependencySet.this.matching((Spec) spec);
        }

        @Override
        public DomainObjectSet<Dependency> matching(Closure spec) {
            return (DomainObjectSet) GolangDependencySet.this.matching(spec);
        }

        @Override
        public Action<? super Dependency> whenObjectAdded(Action<? super Dependency> action) {
            return (Action) GolangDependencySet.this.whenObjectAdded(action);
        }

        @Override
        public void whenObjectAdded(Closure action) {
            GolangDependencySet.this.whenObjectAdded(action);
        }

        @Override
        public Action<? super Dependency> whenObjectRemoved(Action<? super Dependency> action) {
            return (Action) GolangDependencySet.this.whenObjectRemoved(action);
        }

        @Override
        public void whenObjectRemoved(Closure action) {
            GolangDependencySet.this.whenObjectRemoved(action);
        }

        @Override
        public void all(Action<? super Dependency> action) {
            GolangDependencySet.this.all(action);
        }

        @Override
        public void all(Closure action) {
            GolangDependencySet.this.all(action);
        }

        @Override
        public Set<Dependency> findAll(Closure spec) {
            return (Set) GolangDependencySet.this.findAll(spec);
        }

        @Override
        public TaskDependency getBuildDependencies() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int size() {
            return GolangDependencySet.this.size();
        }

        @Override
        public boolean isEmpty() {
            return GolangDependencySet.this.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            return GolangDependencySet.this.contains(o);
        }

        @Override
        public Iterator<Dependency> iterator() {
            return (Iterator) GolangDependencySet.this.iterator();
        }

        @Override
        public Object[] toArray() {
            return GolangDependencySet.this.toArray();
        }

        @Override
        public <T> T[] toArray(T[] a) {
            return GolangDependencySet.this.toArray(a);
        }

        @Override
        public boolean add(Dependency dependency) {
            return GolangDependencySet.this.add((GolangDependency) dependency);
        }

        @Override
        public boolean remove(Object o) {
            return GolangDependencySet.this.remove(o);
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return GolangDependencySet.this.contains(c);
        }

        @Override
        public boolean addAll(Collection<? extends Dependency> c) {
            return GolangDependencySet.this.addAll((Collection) c);
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            return GolangDependencySet.this.removeAll(c);
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            return GolangDependencySet.this.retainAll(c);
        }

        @Override
        public void clear() {
            GolangDependencySet.this.clear();
        }
    }

}
