package com.github.blindpirate.gogradle.core;

import com.github.blindpirate.gogradle.core.dependency.AbstractGolangDependency;
import com.github.blindpirate.gogradle.core.dependency.GolangDependency;
import com.github.blindpirate.gogradle.core.dependency.parse.NotationParser;
import groovy.lang.Closure;
import groovy.lang.GroovyObjectSupport;
import groovy.lang.MissingMethodException;
import groovy.lang.Singleton;
import org.gradle.util.CollectionUtils;
import org.gradle.util.ConfigureUtil;

import javax.inject.Inject;
import java.util.List;

@Singleton
public class GolangDependencyHandler extends GroovyObjectSupport {
    private final NotationParser notationParser;
    private final GolangConfigurationManager configurationManager;

    @Inject
    public GolangDependencyHandler(GolangConfigurationManager configurationManager,
                                   NotationParser notationParser) {
        this.configurationManager = configurationManager;
        this.notationParser = notationParser;
    }

    public Object methodMissing(String name, Object args) {
        Object[] argsArray = (Object[]) args;
        GolangConfiguration configuration = configurationManager.getByName(name);
        if (configuration == null) {
            throw new MissingMethodException(name, this.getClass(), argsArray);
        }

        List<?> normalizedArgs = CollectionUtils.flattenCollections(argsArray);
        if (normalizedArgs.size() == 2 && normalizedArgs.get(1) instanceof Closure) {
            doAdd(configuration, normalizedArgs.get(0), (Closure) normalizedArgs.get(1));
        } else if (normalizedArgs.size() == 1) {
            doAdd(configuration, normalizedArgs.get(0), null);
        } else {
            for (Object arg : normalizedArgs) {
                doAdd(configuration, arg, null);
            }
        }
        return null;
    }

    public void add(String configurationName, Object dependencyNotation) {
        add(configurationName, dependencyNotation, null);
    }

    public void add(String configurationName, Object dependencyNotation, Closure configureClosure) {
        doAdd(configurationManager.getByName(configurationName), dependencyNotation, configureClosure);
    }

    private void doAdd(GolangConfiguration configuration, Object dependencyNotation, Closure configureClosure) {
        GolangDependency dependency = create(dependencyNotation, configureClosure);
        configuration.getDependencies().add(dependency);
    }

    public GolangDependency create(Object dependencyNotation, Closure configureClosure) {
        // first level
        GolangDependency dependency = notationParser.parse(dependencyNotation);
        AbstractGolangDependency.class.cast(dependency).setFirstLevel(true);
        return ConfigureUtil.configure(configureClosure, dependency);
    }
}

