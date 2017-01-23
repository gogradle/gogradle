package com.github.blindpirate.gogradle;

import com.github.blindpirate.gogradle.vcs.git.GitRepository;
import groovy.lang.Closure;
import groovy.lang.GroovyObjectSupport;
import org.gradle.api.Action;
import org.gradle.api.DomainObjectCollection;
import org.gradle.api.NamedDomainObjectList;
import org.gradle.api.Namer;
import org.gradle.api.Rule;
import org.gradle.api.artifacts.ArtifactRepositoryContainer;
import org.gradle.api.artifacts.UnknownRepositoryException;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.artifacts.repositories.ArtifactRepository;
import org.gradle.api.artifacts.repositories.FlatDirectoryArtifactRepository;
import org.gradle.api.artifacts.repositories.IvyArtifactRepository;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.specs.Spec;
import org.gradle.util.ConfigureUtil;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.SortedSet;

@Singleton
public class GolangRepositoryHandler extends GroovyObjectSupport implements RepositoryHandler {

    private List<GitRepository> gitRepositories = new ArrayList<>();

    public void git(Closure closure) {
        GitRepository repository = new GitRepository();
        ConfigureUtil.configure(closure, repository);
        gitRepositories.add(repository);
    }

    public Optional<GitRepository> findMatchedRepository(String name, String url) {
        return gitRepositories.stream().filter(repo -> repo.match(name, url)).findFirst();
    }

    public Object methodMissing(String name, Object args) {
        Object[] argsArray = (Object[]) args;

        if (argsArray.length == 1 && argsArray[0] instanceof Closure) {
            ConfigureUtil.configure((Closure) argsArray[0], this);
        }
        return null;
    }

    @Override
    public FlatDirectoryArtifactRepository flatDir(Map<String, ?> args) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FlatDirectoryArtifactRepository flatDir(Closure configureClosure) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FlatDirectoryArtifactRepository flatDir(Action<? super FlatDirectoryArtifactRepository> action) {
        throw new UnsupportedOperationException();
    }

    @Override
    public MavenArtifactRepository jcenter(Action<? super MavenArtifactRepository> action) {
        throw new UnsupportedOperationException();
    }

    @Override
    public MavenArtifactRepository jcenter() {
        throw new UnsupportedOperationException();
    }

    @Override
    public MavenArtifactRepository mavenCentral(Map<String, ?> args) {
        throw new UnsupportedOperationException();
    }

    @Override
    public MavenArtifactRepository mavenCentral() {
        throw new UnsupportedOperationException();
    }

    @Override
    public MavenArtifactRepository mavenLocal() {
        throw new UnsupportedOperationException();
    }

    @Override
    public MavenArtifactRepository maven(Closure closure) {
        throw new UnsupportedOperationException();
    }

    @Override
    public MavenArtifactRepository maven(Action<? super MavenArtifactRepository> action) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IvyArtifactRepository ivy(Closure closure) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IvyArtifactRepository ivy(Action<? super IvyArtifactRepository> action) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int size() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isEmpty() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<ArtifactRepository> iterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object[] toArray() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean add(ArtifactRepository repository) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends ArtifactRepository> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Namer<ArtifactRepository> getNamer() {
        throw new UnsupportedOperationException();
    }

    @Override
    public SortedMap<String, ArtifactRepository> getAsMap() {
        throw new UnsupportedOperationException();
    }

    @Override
    public SortedSet<String> getNames() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ArtifactRepository findByName(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(int index, Collection<? extends ArtifactRepository> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ArtifactRepository get(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ArtifactRepository set(int index, ArtifactRepository element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(int index, ArtifactRepository element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ArtifactRepository remove(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int indexOf(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int lastIndexOf(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ListIterator<ArtifactRepository> listIterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ListIterator<ArtifactRepository> listIterator(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<ArtifactRepository> subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addFirst(ArtifactRepository repository) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addLast(ArtifactRepository repository) {

    }

    @Override
    public ArtifactRepository getByName(String name) throws UnknownRepositoryException {
        throw new UnsupportedOperationException();
    }

    @Override
    public ArtifactRepository getByName(String name, Closure configureClosure) throws UnknownRepositoryException {
        throw new UnsupportedOperationException();
    }

    @Override
    public ArtifactRepository getByName(String name, Action<? super ArtifactRepository> configureAction) throws UnknownRepositoryException {
        throw new UnsupportedOperationException();
    }

    @Override
    public ArtifactRepository getAt(String name) throws UnknownRepositoryException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Rule addRule(Rule rule) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Rule addRule(String description, Closure ruleAction) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Rule> getRules() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <S extends ArtifactRepository> NamedDomainObjectList<S> withType(Class<S> type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <S extends ArtifactRepository> DomainObjectCollection<S> withType(Class<S> type, Action<? super S> configureAction) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <S extends ArtifactRepository> DomainObjectCollection<S> withType(Class<S> type, Closure configureClosure) {
        throw new UnsupportedOperationException();
    }

    @Override
    public NamedDomainObjectList<ArtifactRepository> matching(Spec<? super ArtifactRepository> spec) {
        throw new UnsupportedOperationException();
    }

    @Override
    public NamedDomainObjectList<ArtifactRepository> matching(Closure spec) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Action<? super ArtifactRepository> whenObjectAdded(Action<? super ArtifactRepository> action) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void whenObjectAdded(Closure action) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Action<? super ArtifactRepository> whenObjectRemoved(Action<? super ArtifactRepository> action) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void whenObjectRemoved(Closure action) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void all(Action<? super ArtifactRepository> action) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void all(Closure action) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<ArtifactRepository> findAll(Closure spec) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ArtifactRepositoryContainer configure(Closure cl) {
        throw new UnsupportedOperationException();
    }
}
