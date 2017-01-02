package com.github.blindpirate.gogradle;

import com.github.blindpirate.gogradle.core.BuildConstraintManager;
import com.github.blindpirate.gogradle.core.DefaultBuildConstraintManager;
import com.github.blindpirate.gogradle.core.GolangConfigurationContainer;
import com.github.blindpirate.gogradle.core.cache.CacheManager;
import com.github.blindpirate.gogradle.core.cache.DefaultCacheManager;
import com.github.blindpirate.gogradle.core.dependency.DefaultDependencyRegistry;
import com.github.blindpirate.gogradle.core.dependency.DependencyRegistry;
import com.github.blindpirate.gogradle.core.dependency.parse.DefaultMapNotationParser;
import com.github.blindpirate.gogradle.core.dependency.parse.DefaultNotationConverter;
import com.github.blindpirate.gogradle.core.dependency.parse.DefaultNotationParser;
import com.github.blindpirate.gogradle.core.dependency.parse.GitMapNotationParser;
import com.github.blindpirate.gogradle.core.dependency.parse.GitNotationConverter;
import com.github.blindpirate.gogradle.core.dependency.parse.MapNotationParser;
import com.github.blindpirate.gogradle.core.dependency.parse.NotationConverter;
import com.github.blindpirate.gogradle.core.dependency.parse.NotationParser;
import com.github.blindpirate.gogradle.core.dependency.produce.DefaultDependencyVisitor;
import com.github.blindpirate.gogradle.core.dependency.produce.ExternalDependencyFactory;
import com.github.blindpirate.gogradle.core.dependency.produce.external.godep.GodepDependencyFactory;
import com.github.blindpirate.gogradle.core.dependency.produce.external.gopm.GopmDependencyFactory;
import com.github.blindpirate.gogradle.core.pack.ErrorReportingPackagePathResolver;
import com.github.blindpirate.gogradle.core.pack.GithubPackagePathResolver;
import com.github.blindpirate.gogradle.core.pack.GlobalCachePackagePathResolver;
import com.github.blindpirate.gogradle.core.pack.MetadataPackagePathResolver;
import com.github.blindpirate.gogradle.core.pack.PackagePathResolver;
import com.github.blindpirate.gogradle.core.pack.StandardPackagePathResolver;
import com.github.blindpirate.gogradle.util.CollectionUtils;
import com.github.blindpirate.gogradle.util.logging.DebugLog;
import com.github.blindpirate.gogradle.util.logging.DebugLogMethodInterceptor;
import com.github.blindpirate.gogradle.vcs.Git;
import com.github.blindpirate.gogradle.vcs.VcsAccessor;
import com.github.blindpirate.gogradle.vcs.git.GitAccessor;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.matcher.Matchers;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.internal.reflect.Instantiator;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.lang.reflect.Method;
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
        bind(CacheManager.class).to(DefaultCacheManager.class);
        bind(ConfigurationContainer.class).to(GolangConfigurationContainer.class);
        bind(DependencyRegistry.class).to(DefaultDependencyRegistry.class);
        bind(PackagePathResolver.class).to(ErrorReportingPackagePathResolver.class);
        bind(NotationConverter.class).to(DefaultNotationConverter.class);
        bind(BuildConstraintManager.class).to(DefaultBuildConstraintManager.class);

        bind(MapNotationParser.class).annotatedWith(Git.class).to(GitMapNotationParser.class);
        bind(NotationConverter.class).annotatedWith(Git.class).to(GitNotationConverter.class);
        bind(VcsAccessor.class).annotatedWith(Git.class).to(GitAccessor.class);

        bindInterceptor(Matchers.any(), new AbstractMatcher<Method>() {
            @Override
            public boolean matches(Method method) {
                return method.isAnnotationPresent(DebugLog.class)
                        && !method.isSynthetic();
            }
        }, new DebugLogMethodInterceptor());


    }


    @Inject
    @Provides
    @Singleton
    @DefaultDependencyVisitor.ExternalDependencyFactories
    public List<ExternalDependencyFactory> externalDependencyFactories(
            GodepDependencyFactory godepDependencyFactory,
            GopmDependencyFactory gopmDependencyFactory) {
        return CollectionUtils.immutableList(
                godepDependencyFactory,
                gopmDependencyFactory);
    }

    @Inject
    @Provides
    @Singleton
    @ErrorReportingPackagePathResolver.PackagePathResolvers
    public List<PackagePathResolver> packagePathResolvers(
            GithubPackagePathResolver githubPackagePathResolver,
            StandardPackagePathResolver standardPackagePathResolver,
            GlobalCachePackagePathResolver globalCachePackagePathResolver,
            MetadataPackagePathResolver metadataPackagePathResolver) {
        return CollectionUtils.immutableList(
                standardPackagePathResolver,
                githubPackagePathResolver,
                globalCachePackagePathResolver,
                metadataPackagePathResolver);
    }


}
