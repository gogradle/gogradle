package com.github.blindpirate.gogradle.core.dependency;

import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.DomainObjectCollection;
import org.gradle.api.NamedDomainObjectSet;
import org.gradle.api.Namer;
import org.gradle.api.Rule;
import org.gradle.api.UnknownDomainObjectException;
import org.gradle.api.specs.Spec;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

public class GolangDependencySet implements NamedDomainObjectSet<GolangPackageDependency> {

    private Set<GolangPackageDependency> dependencies = new HashSet<>();

    public static GolangDependencySet emptySet() {
        return new GolangDependencySet();
    }

    @Override
    public <S extends GolangPackageDependency> NamedDomainObjectSet<S> withType(Class<S> type) {
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
    public Iterator<GolangPackageDependency> iterator() {
        return dependencies.iterator();
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
    public boolean add(GolangPackageDependency e) {
        return dependencies.add(e);
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
    public boolean addAll(Collection<? extends GolangPackageDependency> c) {
        return dependencies.addAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return dependencies.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return dependencies.retainAll(c);
    }

    @Override
    public void clear() {
        dependencies.clear();
    }

    @Override
    public Namer<GolangPackageDependency> getNamer() {
        return null;
    }

    @Override
    public SortedMap<String, GolangPackageDependency> getAsMap() {
        return null;
    }

    @Override
    public SortedSet<String> getNames() {
        return null;
    }

    @Override
    public GolangPackageDependency findByName(String name) {
        return null;
    }

    @Override
    public GolangPackageDependency getByName(String name) throws UnknownDomainObjectException {
        return null;
    }

    @Override
    public GolangPackageDependency getByName(String name,
                                             Closure configureClosure)
            throws UnknownDomainObjectException {
        return null;
    }

    @Override
    public GolangPackageDependency getByName(String name,
                                             Action<? super GolangPackageDependency> configureAction)
            throws UnknownDomainObjectException {
        return null;
    }

    @Override
    public GolangPackageDependency getAt(String name) throws UnknownDomainObjectException {
        return null;
    }

    @Override
    public Rule addRule(Rule rule) {
        return null;
    }

    @Override
    public Rule addRule(String description, Closure ruleAction) {
        return null;
    }

    @Override
    public List<Rule> getRules() {
        return null;
    }

    @Override
    public <S extends GolangPackageDependency> DomainObjectCollection<S> withType(Class<S> type,
                                                                                  Action<? super S> configureAction) {
        return null;
    }

    @Override
    public <S extends GolangPackageDependency> DomainObjectCollection<S> withType(Class<S> type,
                                                                                  Closure configureClosure) {
        return null;
    }

    @Override
    public NamedDomainObjectSet<GolangPackageDependency> matching(Spec<? super GolangPackageDependency> spec) {
        return null;
    }

    @Override
    public NamedDomainObjectSet<GolangPackageDependency> matching(Closure spec) {
        return null;
    }

    @Override
    public Action<? super GolangPackageDependency> whenObjectAdded(Action<? super GolangPackageDependency> action) {
        return null;
    }

    @Override
    public void whenObjectAdded(Closure action) {

    }

    @Override
    public Action<? super GolangPackageDependency> whenObjectRemoved(Action<? super GolangPackageDependency> action) {
        return null;
    }

    @Override
    public void whenObjectRemoved(Closure action) {

    }

    @Override
    public void all(Action<? super GolangPackageDependency> action) {

    }

    @Override
    public void all(Closure action) {

    }

    @Override
    public Set<GolangPackageDependency> findAll(Closure spec) {
        return null;
    }
}
