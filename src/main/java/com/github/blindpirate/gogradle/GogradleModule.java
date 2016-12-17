package com.github.blindpirate.gogradle;

import com.github.blindpirate.gogradle.core.cache.CacheManager;
import com.github.blindpirate.gogradle.core.cache.DefaultCacheManager;
import com.github.blindpirate.gogradle.core.dependency.DefaultDependencyRegistry;
import com.github.blindpirate.gogradle.core.dependency.DependencyRegistry;
import com.github.blindpirate.gogradle.core.dependency.GolangConfigurationContainer;
import com.github.blindpirate.gogradle.core.dependency.external.godep.GodepDependencyFactory;
import com.github.blindpirate.gogradle.core.dependency.parse.DefaultMapNotationParser;
import com.github.blindpirate.gogradle.core.dependency.parse.DefaultNotationParser;
import com.github.blindpirate.gogradle.core.dependency.parse.DefaultStringNotationParser;
import com.github.blindpirate.gogradle.core.dependency.parse.DirMapNotationParser;
import com.github.blindpirate.gogradle.core.dependency.parse.GithubNotationParser;
import com.github.blindpirate.gogradle.core.dependency.parse.MapNotationParser;
import com.github.blindpirate.gogradle.core.dependency.parse.NotationParser;
import com.github.blindpirate.gogradle.core.dependency.parse.StringNotationParser;
import com.github.blindpirate.gogradle.core.dependency.parse.VcsMapNotaionParser;
import com.github.blindpirate.gogradle.core.dependency.resolve.DefaultDependencyFactory;
import com.github.blindpirate.gogradle.core.dependency.resolve.DependencyFactory;
import com.github.blindpirate.gogradle.vcs.BitbucketPackageFetcher;
import com.github.blindpirate.gogradle.vcs.DefaultPackageFetcher;
import com.github.blindpirate.gogradle.vcs.GitPackageFetcher;
import com.github.blindpirate.gogradle.vcs.GithubPackageFetcher;
import com.github.blindpirate.gogradle.vcs.JazzFetcher;
import com.github.blindpirate.gogradle.vcs.LaunchpadFetcher;
import com.github.blindpirate.gogradle.vcs.PackageFetcher;
import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Names;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.internal.reflect.Instantiator;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Map;

import static com.github.blindpirate.gogradle.util.CollectionUtils.immutableList;
import static com.github.blindpirate.gogradle.vcs.VcsType.Git;

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
        bind(NotationParser.class).to(DefaultNotationParser.class);
        bind(MapNotationParser.class).to(DefaultMapNotationParser.class);
        bind(DependencyFactory.class).to(DefaultDependencyFactory.class);
        bind(CacheManager.class).to(DefaultCacheManager.class);
        bind(Instantiator.class).toInstance(instantiator);
        bind(ConfigurationContainer.class).to(GolangConfigurationContainer.class);
        bind(DependencyRegistry.class).to(DefaultDependencyRegistry.class);

        bind(PackageFetcher.class).annotatedWith(Names.named(Git.toString()))
                .to(GitPackageFetcher.class);
    }

    @Inject
    @Provides
    @Singleton
    @DefaultNotationParser.GolangDependencyNotationParsers
    public List<? extends NotationParser> notationParsers(
            DefaultStringNotationParser stringNotationParser,
            DefaultMapNotationParser mapNotationParser) {
        return immutableList(
                stringNotationParser,
                mapNotationParser);
    }

    @Inject
    @Provides
    @Singleton
    @DefaultStringNotationParser.StringNotationParsers
    public List<? extends StringNotationParser> stringNotationParsers(
            GithubNotationParser githubNotationParser) {
        return immutableList(githubNotationParser);
    }


    @Inject
    @Provides
    @Singleton
    @DefaultMapNotationParser.MapNotationParsers
    public List<? extends MapNotationParser> mapNotationParsers(
            DirMapNotationParser dirMapNotationParser,
            VcsMapNotaionParser vcsMapNotaionParser,
            GithubNotationParser githubNotationParser) {
        return immutableList(
                dirMapNotationParser,
                vcsMapNotaionParser,
                githubNotationParser);
    }

    @Inject
    @Provides
    @Singleton
    @DefaultDependencyFactory.ExternalDependencyFactories
    public List<? extends DependencyFactory> externalDependencyFactories(
            GodepDependencyFactory godepDependencyFactory) {
        return immutableList(godepDependencyFactory);
    }

    @Inject
    @Provides
    @Singleton
    @DefaultPackageFetcher.KnownHostPackageFetchers
    public Map<String, PackageFetcher> knownHostPackageFetchers(
            BitbucketPackageFetcher bitbucketPackageFetcher,
            GithubPackageFetcher githubPackageFetcher,
            LaunchpadFetcher launchpadFetcher,
            JazzFetcher jazzFetcher) {
        return ImmutableMap.of(
                "bitbucket.org", bitbucketPackageFetcher,
                "github.com", githubPackageFetcher,
                "launchpad.net", launchpadFetcher,
                "hub.jazz.net", jazzFetcher
        );
    }

}
