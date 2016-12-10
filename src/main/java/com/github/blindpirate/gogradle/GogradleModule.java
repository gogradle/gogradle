package com.github.blindpirate.gogradle;

import com.github.blindpirate.gogradle.core.cache.CacheManager;
import com.github.blindpirate.gogradle.core.cache.DefaultCacheManager;
import com.github.blindpirate.gogradle.core.dependency.GolangConfigurationContainer;
import com.github.blindpirate.gogradle.core.dependency.parse.DefaultGolangDependencyParser;
import com.github.blindpirate.gogradle.core.dependency.parse.DefaultMapNotationoParser;
import com.github.blindpirate.gogradle.core.dependency.parse.DefaultStringNotationParser;
import com.github.blindpirate.gogradle.core.dependency.parse.GitNotationParser;
import com.github.blindpirate.gogradle.core.dependency.parse.GithubNotationParser;
import com.github.blindpirate.gogradle.core.dependency.parse.MapNotationParser;
import com.github.blindpirate.gogradle.core.dependency.parse.NotationParser;
import com.github.blindpirate.gogradle.core.dependency.parse.StringNotationParser;
import com.github.blindpirate.gogradle.vcs.GitPackageFetcher;
import com.github.blindpirate.gogradle.vcs.PackageFetcher;
import com.github.blindpirate.gogradle.vcs.VcsType;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Names;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.internal.reflect.Instantiator;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

import static com.github.blindpirate.gogradle.util.CollectionUtils.immutableList;

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
        bind(NotationParser.class).to(DefaultGolangDependencyParser.class);
        bind(CacheManager.class).to(DefaultCacheManager.class);
        bind(Instantiator.class).toInstance(instantiator);
        bind(ConfigurationContainer.class).to(GolangConfigurationContainer.class);

        bind(NotationParser.class).annotatedWith(Names.named(VcsType.Git.toString()))
                .to(GitNotationParser.class);
        bind(PackageFetcher.class).annotatedWith(Names.named(VcsType.Git.toString()))
                .to(GitPackageFetcher.class);
    }

    @Inject
    @Provides
    @Singleton
    @DefaultGolangDependencyParser.GolangDependencyNotationParsers
    public List<? extends NotationParser> notationParsers(
            DefaultStringNotationParser stringNotationParser,
            DefaultMapNotationoParser mapNotationParser) {
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
    @DefaultMapNotationoParser.MapNotationParsers
    public List<? extends MapNotationParser> mapNoataionParsers(
            GithubNotationParser githubNotationParser) {
        return immutableList(githubNotationParser);
    }

    @Inject
    @Provides
    @Singleton
    @GitNotationParser.GitNotationParsers
    public List<? extends MapNotationParser> gitNotationParsers(
            GithubNotationParser githubNotationParser) {
        return immutableList(githubNotationParser);
    }
}
