package com.github.blindpirate.gogradle.core;

import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.internal.AbstractNamedDomainObjectContainer;
import org.gradle.internal.reflect.Instantiator;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.gradle.api.artifacts.Configuration.Namer;

@Singleton
public class GolangConfigurationContainer extends AbstractNamedDomainObjectContainer<Configuration>
        implements ConfigurationContainer {

    private final Instantiator instantiator;

    @Inject
    public GolangConfigurationContainer(Instantiator instantiator) {
        super(Configuration.class, instantiator, new Namer());
        this.instantiator = instantiator;
    }

    @Override
    protected GolangConfiguration doCreate(String name) {
        return instantiator.newInstance(GolangConfiguration.class, name);
    }

    @Override
    public GolangConfiguration detachedConfiguration(Dependency... dependencies) {
        return null;
    }
}
