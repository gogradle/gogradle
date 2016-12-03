package com.github.blindpirate.gogradle.core.dependency;

import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.DomainObjectCollection;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.TaskDependency;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class GolangDependencySet implements DependencySet {

    private Set<GolangDependency> dependencies = new HashSet<>();

    public static GolangDependencySet emptySet() {
        return new GolangDependencySet();
    }

    @Override
    public <S extends Dependency> DomainObjectSet<S> withType(Class<S> type) {
        return null;
    }

    @Override
    public <S extends Dependency> DomainObjectCollection<S> withType(Class<S> type, Action<? super S> configureAction) {
        return null;
    }

    @Override
    public <S extends Dependency> DomainObjectCollection<S> withType(Class<S> type, Closure configureClosure) {
        return null;
    }

    @Override
    public DomainObjectSet<Dependency> matching(Spec<? super Dependency> spec) {
        return null;
    }

    @Override
    public DomainObjectSet<Dependency> matching(Closure spec) {
        return null;
    }

    @Override
    public Action<? super Dependency> whenObjectAdded(Action<? super Dependency> action) {
        return null;
    }

    @Override
    public void whenObjectAdded(Closure action) {

    }

    @Override
    public Action<? super Dependency> whenObjectRemoved(Action<? super Dependency> action) {
        return null;
    }

    @Override
    public void whenObjectRemoved(Closure action) {

    }

    @Override
    public void all(Action<? super Dependency> action) {

    }

    @Override
    public void all(Closure action) {

    }

    @Override
    public Set<Dependency> findAll(Closure spec) {
        return null;
    }

    @Override
    public TaskDependency getBuildDependencies() {
        return null;
    }

    @Override
    public int size() {
        return dependencies.size();
    }

    @Override
    public boolean isEmpty() {
        return dependencies.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return dependencies.contains(o);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Iterator<Dependency> iterator() {
        return (Iterator) dependencies.iterator();
    }

    @Override
    public Object[] toArray() {
        return dependencies.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return dependencies.toArray(a);
    }

    @Override
    public boolean add(Dependency dependency) {
        return dependencies.add((GolangDependency) dependency);
    }

    @Override
    public boolean remove(Object o) {
        return dependencies.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return dependencies.containsAll(c);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean addAll(Collection<? extends Dependency> c) {
        return dependencies.addAll((Collection) c);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean removeAll(Collection<?> c) {
        return dependencies.removeAll((Collection) c);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean retainAll(Collection<?> c) {
        return dependencies.retainAll((Collection) c);
    }

    @Override
    public void clear() {
        dependencies.clear();
    }
}
