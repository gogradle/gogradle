package com.github.blindpirate.gogradle.core.dependency;

import com.github.blindpirate.gogradle.core.dependency.parse.DefaultGolangDependencyParser;
import groovy.lang.Closure;
import groovy.lang.GroovyObjectSupport;
import groovy.lang.MissingMethodException;
import org.gradle.api.Action;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.dsl.ComponentMetadataHandler;
import org.gradle.api.artifacts.dsl.ComponentModuleMetadataHandler;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.artifacts.query.ArtifactResolutionQuery;
import org.gradle.internal.Cast;
import org.gradle.util.CollectionUtils;
import org.gradle.util.ConfigureUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Map;

@Singleton
public class GolangDependencyHandler extends GroovyObjectSupport implements DependencyHandler {

    private final ConfigurationContainer configurationContainer;

    private final DefaultGolangDependencyParser dependencyParser;

    @Inject
    public GolangDependencyHandler(ConfigurationContainer configurationContainer,
                                   DefaultGolangDependencyParser dependencyParser) {
        this.configurationContainer = configurationContainer;
        this.dependencyParser = dependencyParser;
    }

    public Dependency add(String configurationName, Object dependencyNotation) {
        return add(configurationName, dependencyNotation, null);
    }

    public Dependency add(String configurationName, Object dependencyNotation, Closure configureClosure) {
        return doAdd(configurationContainer.findByName(configurationName), dependencyNotation, configureClosure);
    }

    private Dependency doAdd(Configuration configuration, Object dependencyNotation, Closure configureClosure) {
        Dependency dependency = create(dependencyNotation, configureClosure);
        configuration.getDependencies().add(dependency);
        return dependency;
    }

    public Dependency create(Object dependencyNotation) {
        return create(dependencyNotation, null);
    }

    @Override
    public Dependency create(Object dependencyNotation, Closure configureClosure) {
        // first level
        Dependency dependency = dependencyParser.produce(dependencyNotation);
        Cast.cast(AbstractGolangDependency.class, dependency).setFirstLevel(true);
        return ConfigureUtil.configure(configureClosure, dependency);
    }

    public Object methodMissing(String name, Object args) {
        Object[] argsArray = (Object[]) args;
        Configuration configuration = configurationContainer.findByName(name);
        if (configuration == null) {
            throw new MissingMethodException(name, this.getClass(), argsArray);
        }

        List<?> normalizedArgs = CollectionUtils.flattenCollections(argsArray);
        if (normalizedArgs.size() == 2 && normalizedArgs.get(1) instanceof Closure) {
            return doAdd(configuration, normalizedArgs.get(0), (Closure) normalizedArgs.get(1));
        } else if (normalizedArgs.size() == 1) {
            return doAdd(configuration, normalizedArgs.get(0), null);
        } else {
            for (Object arg : normalizedArgs) {
                doAdd(configuration, arg, null);
            }
            return null;
        }
    }

    @Override
    public Dependency module(Object notation) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Dependency module(Object notation, Closure configureClosure) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Dependency project(Map<String, ?> notation) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Dependency gradleApi() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Dependency gradleTestKit() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Dependency localGroovy() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ComponentMetadataHandler getComponents() {
        return null;
    }

    @Override
    public void components(Action<? super ComponentMetadataHandler> configureAction) {

    }

    @Override
    public ComponentModuleMetadataHandler getModules() {
        return null;
    }

    @Override
    public void modules(Action<? super ComponentModuleMetadataHandler> configureAction) {

    }

    @Override
    public ArtifactResolutionQuery createArtifactResolutionQuery() {
        return null;
    }
}
