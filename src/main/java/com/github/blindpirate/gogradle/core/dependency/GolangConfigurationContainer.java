package com.github.blindpirate.gogradle.core.dependency;

import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.DomainObjectCollection;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.NamedDomainObjectSet;
import org.gradle.api.Namer;
import org.gradle.api.Rule;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.UnknownConfigurationException;
import org.gradle.api.specs.Spec;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

public class GolangConfigurationContainer implements ConfigurationContainer {
    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean contains(Object o) {
        return false;
    }

    @Override
    public Iterator<Configuration> iterator() {
        return null;
    }

    @Override
    public Object[] toArray() {
        return new Object[0];
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return null;
    }

    @Override
    public boolean add(Configuration e) {
        return false;
    }

    @Override
    public boolean remove(Object o) {
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean addAll(Collection<? extends Configuration> c) {
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return false;
    }

    @Override
    public void clear() {

    }

    @Override
    public Namer<Configuration> getNamer() {
        return null;
    }

    @Override
    public SortedMap<String, Configuration> getAsMap() {
        return null;
    }

    @Override
    public SortedSet<String> getNames() {
        return null;
    }

    @Override
    public Configuration findByName(String name) {
        return null;
    }

    @Override
    public Configuration getByName(String name) throws UnknownConfigurationException {
        return null;
    }

    @Override
    public Configuration getAt(String name) throws UnknownConfigurationException {
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
    public Configuration getByName(String name, Closure configureClosure) throws UnknownConfigurationException {
        return null;
    }

    @Override
    public Configuration getByName(String name,
                                   Action<? super Configuration> configureAction)
            throws UnknownConfigurationException {
        return null;
    }

    @Override
    public Configuration detachedConfiguration(Dependency... dependencies) {
        return null;
    }

    @Override
    public Configuration create(String name) throws InvalidUserDataException {
        return null;
    }

    @Override
    public Configuration maybeCreate(String name) {
        return null;
    }

    @Override
    public Configuration create(String name, Closure configureClosure) throws InvalidUserDataException {
        return null;
    }

    @Override
    public Configuration create(String name,
                                Action<? super Configuration> configureAction)
            throws InvalidUserDataException {
        return null;
    }

    @Override
    public NamedDomainObjectContainer<Configuration> configure(Closure configureClosure) {
        return null;
    }

    @Override
    public <S extends Configuration> NamedDomainObjectSet<S> withType(Class<S> type) {
        return null;
    }

    @Override
    public <S extends Configuration> DomainObjectCollection<S> withType(Class<S> type,
                                                                        Action<? super S> configureAction) {
        return null;
    }

    @Override
    public <S extends Configuration> DomainObjectCollection<S> withType(Class<S> type, Closure configureClosure) {
        return null;
    }

    @Override
    public NamedDomainObjectSet<Configuration> matching(Spec<? super Configuration> spec) {
        return null;
    }

    @Override
    public NamedDomainObjectSet<Configuration> matching(Closure spec) {
        return null;
    }

    @Override
    public Action<? super Configuration> whenObjectAdded(Action<? super Configuration> action) {
        return null;
    }

    @Override
    public void whenObjectAdded(Closure action) {

    }

    @Override
    public Action<? super Configuration> whenObjectRemoved(Action<? super Configuration> action) {
        return null;
    }

    @Override
    public void whenObjectRemoved(Closure action) {

    }

    @Override
    public void all(Action<? super Configuration> action) {

    }

    @Override
    public void all(Closure action) {

    }

    @Override
    public Set<Configuration> findAll(Closure spec) {
        return null;
    }
}
