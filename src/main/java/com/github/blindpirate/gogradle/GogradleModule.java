package com.github.blindpirate.gogradle;

import com.github.blindpirate.gogradle.core.cache.CacheManager;
import com.github.blindpirate.gogradle.core.cache.DefaultCacheManager;
import com.github.blindpirate.gogradle.core.dependency.DefaultDependencyRegistry;
import com.github.blindpirate.gogradle.core.dependency.DependencyRegistry;
import com.github.blindpirate.gogradle.core.dependency.GolangConfigurationContainer;
import com.github.blindpirate.gogradle.core.dependency.external.godep.GodepDependencyFactory;
import com.github.blindpirate.gogradle.core.dependency.external.gopm.GopmDependencyFactory;
import com.github.blindpirate.gogradle.core.dependency.parse.DefaultMapNotationParser;
import com.github.blindpirate.gogradle.core.dependency.parse.DefaultNotationConverter;
import com.github.blindpirate.gogradle.core.dependency.parse.DefaultNotationParser;
import com.github.blindpirate.gogradle.core.dependency.parse.GitMapNotationParser;
import com.github.blindpirate.gogradle.core.dependency.parse.GitNotationConverter;
import com.github.blindpirate.gogradle.core.dependency.parse.MapNotationParser;
import com.github.blindpirate.gogradle.core.dependency.parse.NotationConverter;
import com.github.blindpirate.gogradle.core.dependency.parse.NotationParser;
import com.github.blindpirate.gogradle.core.dependency.resolve.DefaultDependencyFactory;
import com.github.blindpirate.gogradle.core.dependency.resolve.DependencyFactory;
import com.github.blindpirate.gogradle.core.pack.DefaultPackageNameResolver;
import com.github.blindpirate.gogradle.core.pack.GithubPackageNameResolver;
import com.github.blindpirate.gogradle.core.pack.GlobalCachePackageNameResolver;
import com.github.blindpirate.gogradle.core.pack.MetadataPackageNameResolver;
import com.github.blindpirate.gogradle.core.pack.PackageNameResolver;
import com.github.blindpirate.gogradle.core.pack.StandardPackageNameResolver;
import com.github.blindpirate.gogradle.util.CollectionUtils;
import com.github.blindpirate.gogradle.vcs.Git;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.internal.reflect.Instantiator;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

/**
 * Provides configurations for Guice dependency injection.
 */
public class GogradleModule extends AbstractModule {
    private final Instantiator instantiator;

    public GogradleModule(Instantiator instantiator) {
        this.instantiator = instantiator;
    }

    @Override
    protected void configure() {
        bind(Instantiator.class).toInstance(instantiator);

        bind(NotationParser.class).to(DefaultNotationParser.class);
        bind(MapNotationParser.class).to(DefaultMapNotationParser.class);
        bind(DependencyFactory.class).to(DefaultDependencyFactory.class);
        bind(CacheManager.class).to(DefaultCacheManager.class);
        bind(ConfigurationContainer.class).to(GolangConfigurationContainer.class);
        bind(DependencyRegistry.class).to(DefaultDependencyRegistry.class);
        bind(PackageNameResolver.class).to(DefaultPackageNameResolver.class);
        bind(NotationConverter.class).to(DefaultNotationConverter.class);

        bind(MapNotationParser.class).annotatedWith(Git.class).to(GitMapNotationParser.class);
        bind(NotationConverter.class).annotatedWith(Git.class).to(GitNotationConverter.class);

    }


    @Inject
    @Provides
    @Singleton
    @DefaultDependencyFactory.ExternalDependencyFactories
    public List<DependencyFactory> externalDependencyFactories(
            GodepDependencyFactory godepDependencyFactory,
            GopmDependencyFactory gopmDependencyFactory) {
        return CollectionUtils.<DependencyFactory>immutableList(
                godepDependencyFactory,
                gopmDependencyFactory);
    }

    @Inject
    @Provides
    @Singleton
    @DefaultPackageNameResolver.PackageNameResolvers
    public List<PackageNameResolver> packageNameResolvers(
            GithubPackageNameResolver githubPackageNameResolver,
            StandardPackageNameResolver standardPackageNameResolver,
            GlobalCachePackageNameResolver globalCachePackageNameResolver,
            MetadataPackageNameResolver metadataPackageNameResolver) {
        return CollectionUtils.immutableList(
                standardPackageNameResolver,
                githubPackageNameResolver,
                globalCachePackageNameResolver,
                metadataPackageNameResolver);
    }


}
