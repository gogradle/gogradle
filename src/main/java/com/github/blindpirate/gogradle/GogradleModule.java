package com.github.blindpirate.gogradle;

import com.github.blindpirate.gogradle.build.BuildManager;
import com.github.blindpirate.gogradle.build.DefaultBuildManager;
import com.github.blindpirate.gogradle.core.BuildConstraintManager;
import com.github.blindpirate.gogradle.core.DefaultBuildConstraintManager;
import com.github.blindpirate.gogradle.core.GolangConfigurationContainer;
import com.github.blindpirate.gogradle.core.cache.DefaultGlobalCacheManager;
import com.github.blindpirate.gogradle.core.cache.GlobalCacheManager;
import com.github.blindpirate.gogradle.core.dependency.DefaultDependencyRegistry;
import com.github.blindpirate.gogradle.core.dependency.DependencyRegistry;
import com.github.blindpirate.gogradle.core.dependency.lock.DefaultLockedDependencyManager;
import com.github.blindpirate.gogradle.core.dependency.lock.LockedDependencyManager;
import com.github.blindpirate.gogradle.core.dependency.parse.DefaultMapNotationParser;
import com.github.blindpirate.gogradle.core.dependency.parse.DefaultNotationConverter;
import com.github.blindpirate.gogradle.core.dependency.parse.DefaultNotationParser;
import com.github.blindpirate.gogradle.core.dependency.parse.GitMapNotationParser;
import com.github.blindpirate.gogradle.core.dependency.parse.GitNotationConverter;
import com.github.blindpirate.gogradle.core.dependency.parse.MapNotationParser;
import com.github.blindpirate.gogradle.core.dependency.parse.NotationConverter;
import com.github.blindpirate.gogradle.core.dependency.parse.NotationParser;
import com.github.blindpirate.gogradle.core.dependency.produce.DefaultDependencyVisitor;
import com.github.blindpirate.gogradle.core.dependency.produce.DependencyVisitor;
import com.github.blindpirate.gogradle.core.dependency.produce.ExternalDependencyFactory;
import com.github.blindpirate.gogradle.core.dependency.produce.external.glide.GlideDependencyFactory;
import com.github.blindpirate.gogradle.core.dependency.produce.external.glock.GlockDependencyFactory;
import com.github.blindpirate.gogradle.core.dependency.produce.external.godep.GodepDependencyFactory;
import com.github.blindpirate.gogradle.core.dependency.produce.external.gopm.GopmDependencyFactory;
import com.github.blindpirate.gogradle.core.dependency.produce.external.govendor.GovendorDependencyFactory;
import com.github.blindpirate.gogradle.core.dependency.produce.external.gvtgbvendor.GvtGbvendorDependencyFactory;
import com.github.blindpirate.gogradle.core.dependency.produce.external.trash.TrashDependencyFactory;
import com.github.blindpirate.gogradle.core.pack.DefaultPackagePathResolver;
import com.github.blindpirate.gogradle.core.pack.GithubPackagePathResolver;
import com.github.blindpirate.gogradle.core.pack.GlobalCachePackagePathResolver;
import com.github.blindpirate.gogradle.core.pack.MetadataPackagePathResolver;
import com.github.blindpirate.gogradle.core.pack.PackagePathResolver;
import com.github.blindpirate.gogradle.core.pack.StandardPackagePathResolver;
import com.github.blindpirate.gogradle.core.pack.UnrecognizedPackagePathResolver;
import com.github.blindpirate.gogradle.crossplatform.DefaultGoBinaryManager;
import com.github.blindpirate.gogradle.crossplatform.GoBinaryManager;
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
import org.gradle.api.Project;
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
    private final Project project;
    private final Instantiator instantiator;

    public GogradleModule(Project project, Instantiator instantiator) {
        this.project = project;
        this.instantiator = instantiator;
    }

    @Override
    protected void configure() {
        bind(Project.class).toInstance(project);
        bind(Instantiator.class).toInstance(instantiator);

        bind(NotationParser.class).to(DefaultNotationParser.class);
        bind(BuildManager.class).to(DefaultBuildManager.class);
        bind(GoBinaryManager.class).to(DefaultGoBinaryManager.class);
        bind(MapNotationParser.class).to(DefaultMapNotationParser.class);
        bind(GlobalCacheManager.class).to(DefaultGlobalCacheManager.class);
        bind(ConfigurationContainer.class).to(GolangConfigurationContainer.class);
        bind(DependencyRegistry.class).to(DefaultDependencyRegistry.class);
        bind(PackagePathResolver.class).to(DefaultPackagePathResolver.class);
        bind(NotationConverter.class).to(DefaultNotationConverter.class);
        bind(BuildConstraintManager.class).to(DefaultBuildConstraintManager.class);
        bind(DependencyVisitor.class).to(DefaultDependencyVisitor.class);
        bind(LockedDependencyManager.class).to(DefaultLockedDependencyManager.class);

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


    // The order is based on https://github.com/blindpirate/report-of-go-package-management-tool
    @Inject
    @Provides
    @Singleton
    @DefaultDependencyVisitor.ExternalDependencyFactories
    public List<ExternalDependencyFactory> externalDependencyFactories(
            DefaultLockedDependencyManager lockedDependencyManager,
            GodepDependencyFactory godepDependencyFactory,
            GlideDependencyFactory glideDependencyFactory,
            GovendorDependencyFactory govendorDependencyFactory,
            GvtGbvendorDependencyFactory gvtGbvendorDependencyFactory,
            TrashDependencyFactory trashDependencyFactory,
            GlockDependencyFactory glockDependencyFactory,
            GopmDependencyFactory gopmDependencyFactory) {
        return CollectionUtils.immutableList(
                lockedDependencyManager,
                godepDependencyFactory,
                glideDependencyFactory,
                govendorDependencyFactory,
                gvtGbvendorDependencyFactory,
                trashDependencyFactory,
                glockDependencyFactory,
                gopmDependencyFactory);

    }

    @Inject
    @Provides
    @Singleton
    @DefaultPackagePathResolver.PackagePathResolvers
    public List<PackagePathResolver> packagePathResolvers(
            GithubPackagePathResolver githubPackagePathResolver,
            StandardPackagePathResolver standardPackagePathResolver,
            GlobalCachePackagePathResolver globalCachePackagePathResolver,
            MetadataPackagePathResolver metadataPackagePathResolver,
            UnrecognizedPackagePathResolver unrecognizedPackagePathResolver) {
        return CollectionUtils.immutableList(
                standardPackagePathResolver,
                githubPackagePathResolver,
                globalCachePackagePathResolver,
                metadataPackagePathResolver,
                unrecognizedPackagePathResolver
        );
    }


}
