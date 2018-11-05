/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.github.blindpirate.gogradle;

import com.github.blindpirate.gogradle.build.BuildManager;
import com.github.blindpirate.gogradle.build.DefaultBuildManager;
import com.github.blindpirate.gogradle.core.BuildConstraintManager;
import com.github.blindpirate.gogradle.core.DefaultBuildConstraintManager;
import com.github.blindpirate.gogradle.core.cache.DefaultGlobalCacheManager;
import com.github.blindpirate.gogradle.core.cache.GlobalCacheManager;
import com.github.blindpirate.gogradle.core.dependency.lock.DefaultLockedDependencyManager;
import com.github.blindpirate.gogradle.core.dependency.lock.LockedDependencyManager;
import com.github.blindpirate.gogradle.core.dependency.parse.DefaultMapNotationParser;
import com.github.blindpirate.gogradle.core.dependency.parse.DefaultNotationConverter;
import com.github.blindpirate.gogradle.core.dependency.parse.DefaultNotationParser;
import com.github.blindpirate.gogradle.core.dependency.parse.GitMercurialMapNotationParser;
import com.github.blindpirate.gogradle.core.dependency.parse.GitMercurialNotationConverter;
import com.github.blindpirate.gogradle.core.dependency.parse.MapNotationParser;
import com.github.blindpirate.gogradle.core.dependency.parse.NotationConverter;
import com.github.blindpirate.gogradle.core.dependency.parse.NotationParser;
import com.github.blindpirate.gogradle.core.dependency.produce.DefaultDependencyVisitor;
import com.github.blindpirate.gogradle.core.dependency.produce.DependencyVisitor;
import com.github.blindpirate.gogradle.core.dependency.produce.ExternalDependencyFactory;
import com.github.blindpirate.gogradle.core.dependency.produce.external.dep.DepDependencyFactory;
import com.github.blindpirate.gogradle.core.dependency.produce.external.glide.GlideDependencyFactory;
import com.github.blindpirate.gogradle.core.dependency.produce.external.glock.GlockDependencyFactory;
import com.github.blindpirate.gogradle.core.dependency.produce.external.godep.GodepDependencyFactory;
import com.github.blindpirate.gogradle.core.dependency.produce.external.gopm.GopmDependencyFactory;
import com.github.blindpirate.gogradle.core.dependency.produce.external.govendor.GovendorDependencyFactory;
import com.github.blindpirate.gogradle.core.dependency.produce.external.gpm.GpmDependencyFactory;
import com.github.blindpirate.gogradle.core.dependency.produce.external.gvtgbvendor.GvtGbvendorDependencyFactory;
import com.github.blindpirate.gogradle.core.dependency.produce.external.trash.TrashDependencyFactory;
import com.github.blindpirate.gogradle.core.dependency.resolve.DependencyManager;
import com.github.blindpirate.gogradle.core.pack.BitbucketPackagePathResolver;
import com.github.blindpirate.gogradle.core.pack.DefaultPackagePathResolver;
import com.github.blindpirate.gogradle.core.pack.GithubGitlabPackagePathResolver;
import com.github.blindpirate.gogradle.core.pack.GlobalCachePackagePathResolver;
import com.github.blindpirate.gogradle.core.pack.IBMDevOpsPackagePathResolver;
import com.github.blindpirate.gogradle.core.pack.MetadataPackagePathResolver;
import com.github.blindpirate.gogradle.core.pack.PackagePathResolver;
import com.github.blindpirate.gogradle.core.pack.RepositoryHandlerPathResolver;
import com.github.blindpirate.gogradle.core.pack.StandardPackagePathResolver;
import com.github.blindpirate.gogradle.core.pack.UnrecognizedPackagePathResolver;
import com.github.blindpirate.gogradle.core.pack.VcsPackagePathResolver;
import com.github.blindpirate.gogradle.crossplatform.DefaultGoBinaryManager;
import com.github.blindpirate.gogradle.crossplatform.GoBinaryManager;
import com.github.blindpirate.gogradle.task.go.test.DefaultGoTestResultExtractor;
import com.github.blindpirate.gogradle.task.go.test.GoTestResultExtractor;
import com.github.blindpirate.gogradle.util.logging.DebugLog;
import com.github.blindpirate.gogradle.util.logging.DebugLogMethodInterceptor;
import com.github.blindpirate.gogradle.vcs.Bazaar;
import com.github.blindpirate.gogradle.vcs.Git;
import com.github.blindpirate.gogradle.vcs.Mercurial;
import com.github.blindpirate.gogradle.vcs.Svn;
import com.github.blindpirate.gogradle.vcs.VcsAccessor;
import com.github.blindpirate.gogradle.vcs.bazaar.BazaarAccessor;
import com.github.blindpirate.gogradle.vcs.bazaar.BazaarMapNotationParser;
import com.github.blindpirate.gogradle.vcs.bazaar.BazaarNotationConverter;
import com.github.blindpirate.gogradle.vcs.git.GitClientAccessor;
import com.github.blindpirate.gogradle.vcs.git.GitDependencyManager;
import com.github.blindpirate.gogradle.vcs.mercurial.HgClientAccessor;
import com.github.blindpirate.gogradle.vcs.mercurial.MercurialDependencyManager;
import com.github.blindpirate.gogradle.vcs.svn.SvnAccessor;
import com.github.blindpirate.gogradle.vcs.svn.SvnMapNotationParser;
import com.github.blindpirate.gogradle.vcs.svn.SvnNotationConverter;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.InjectionPoint;
import org.gradle.api.Project;
import org.gradle.api.internal.project.DefaultProject;
import org.gradle.internal.service.ServiceRegistry;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.lang.reflect.Method;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.github.blindpirate.gogradle.util.CollectionUtils.immutableList;

/**
 * Provides configurations for Guice dependency injection.
 */
public class GogradleModule extends AbstractModule {
    private final Project project;
    private final ServiceRegistry serviceRegistry;
    private static final Logger INJECTION_POINT_LOGGER = Logger.getLogger(InjectionPoint.class.getName());

    static {
        INJECTION_POINT_LOGGER.setLevel(Level.OFF);
    }

    public GogradleModule(Project project) {
        this.project = project;
        this.serviceRegistry = DefaultProject.class.cast(project).getServices();
    }

    @Override
    protected void configure() {
        bind(Project.class).toInstance(project);
        bind(ServiceRegistry.class).toInstance(serviceRegistry);

        bind(NotationParser.class).to(DefaultNotationParser.class);
        bind(BuildManager.class).to(DefaultBuildManager.class);
        bind(GoBinaryManager.class).to(DefaultGoBinaryManager.class);
        bind(MapNotationParser.class).to(DefaultMapNotationParser.class);
        bind(GlobalCacheManager.class).to(DefaultGlobalCacheManager.class);
        bind(NotationConverter.class).to(DefaultNotationConverter.class);
        bind(BuildConstraintManager.class).to(DefaultBuildConstraintManager.class);
        bind(DependencyVisitor.class).to(DefaultDependencyVisitor.class);
        bind(LockedDependencyManager.class).to(DefaultLockedDependencyManager.class);
        bind(GoTestResultExtractor.class).to(DefaultGoTestResultExtractor.class);

        bind(MapNotationParser.class).annotatedWith(Git.class).to(GitMercurialMapNotationParser.class);
        bind(NotationConverter.class).annotatedWith(Git.class).to(GitMercurialNotationConverter.class);
        bind(VcsAccessor.class).annotatedWith(Git.class).to(GitClientAccessor.class);
        bind(DependencyManager.class).annotatedWith(Git.class).to(GitDependencyManager.class);

        bind(MapNotationParser.class).annotatedWith(Mercurial.class).to(GitMercurialMapNotationParser.class);
        bind(NotationConverter.class).annotatedWith(Mercurial.class).to(GitMercurialNotationConverter.class);
        bind(VcsAccessor.class).annotatedWith(Mercurial.class).to(HgClientAccessor.class);
        bind(DependencyManager.class).annotatedWith(Mercurial.class).to(MercurialDependencyManager.class);

        bind(MapNotationParser.class).annotatedWith(Svn.class).to(SvnMapNotationParser.class);
        bind(NotationConverter.class).annotatedWith(Svn.class).to(SvnNotationConverter.class);
        bind(VcsAccessor.class).annotatedWith(Svn.class).to(SvnAccessor.class);

        bind(MapNotationParser.class).annotatedWith(Bazaar.class).to(BazaarMapNotationParser.class);
        bind(NotationConverter.class).annotatedWith(Bazaar.class).to(BazaarNotationConverter.class);
        bind(VcsAccessor.class).annotatedWith(Bazaar.class).to(BazaarAccessor.class);

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
            DepDependencyFactory depDependencyFactory,
            GodepDependencyFactory godepDependencyFactory,
            GlideDependencyFactory glideDependencyFactory,
            GovendorDependencyFactory govendorDependencyFactory,
            GvtGbvendorDependencyFactory gvtGbvendorDependencyFactory,
            TrashDependencyFactory trashDependencyFactory,
            GlockDependencyFactory glockDependencyFactory,
            GopmDependencyFactory gopmDependencyFactory,
            GpmDependencyFactory gpmDependencyFactory) {
        return immutableList(
                lockedDependencyManager,
                depDependencyFactory,
                godepDependencyFactory,
                glideDependencyFactory,
                govendorDependencyFactory,
                gvtGbvendorDependencyFactory,
                trashDependencyFactory,
                glockDependencyFactory,
                gopmDependencyFactory,
                gpmDependencyFactory);

    }

    /*
     * PackagePathResolver which supports package substitution.
     * See https://github.com/gogradle/gogradle/blob/master/docs/repository-management.md
     */
    @Inject
    @Provides
    @Singleton
    @DefaultPackagePathResolver.AllPackagePathResolvers
    public PackagePathResolver allPackagePathResolver(
            RepositoryHandlerPathResolver repositoryHandlerPathResolver,
            BitbucketPackagePathResolver bitbucketPackagePathResolver,
            IBMDevOpsPackagePathResolver ibmDevOpsPackagePathResolver,
            StandardPackagePathResolver standardPackagePathResolver,
            GlobalCachePackagePathResolver globalCachePackagePathResolver,
            VcsPackagePathResolver vcsPackagePathResolver,
            MetadataPackagePathResolver metadataPackagePathResolver,
            UnrecognizedPackagePathResolver unrecognizedPackagePathResolver) {
        return new DefaultPackagePathResolver(
                repositoryHandlerPathResolver,
                standardPackagePathResolver,
                new GithubGitlabPackagePathResolver("github.com"),
                new GithubGitlabPackagePathResolver("gitlab.com"),
                bitbucketPackagePathResolver,
                ibmDevOpsPackagePathResolver,
                globalCachePackagePathResolver,
                vcsPackagePathResolver,
                metadataPackagePathResolver,
                unrecognizedPackagePathResolver
        );
    }

    /*
     * PackagePathResolver which doesn't support package substitution.
     * E.g. only produces "original" packages (specified by https://golang.org/cmd/go/#hdr-Remote_import_paths).
     */
    @Inject
    @Provides
    @Singleton
    @DefaultPackagePathResolver.OriginalPackagePathResolvers
    public PackagePathResolver originalPackagePathResolver(
            BitbucketPackagePathResolver bitbucketPackagePathResolver,
            IBMDevOpsPackagePathResolver ibmDevOpsPackagePathResolver,
            StandardPackagePathResolver standardPackagePathResolver,
            GlobalCachePackagePathResolver globalCachePackagePathResolver,
            VcsPackagePathResolver vcsPackagePathResolver,
            MetadataPackagePathResolver metadataPackagePathResolver,
            UnrecognizedPackagePathResolver unrecognizedPackagePathResolver) {
        return new DefaultPackagePathResolver(
                standardPackagePathResolver,
                new GithubGitlabPackagePathResolver("github.com"),
                new GithubGitlabPackagePathResolver("gitlab.com"),
                bitbucketPackagePathResolver,
                ibmDevOpsPackagePathResolver,
                globalCachePackagePathResolver,
                vcsPackagePathResolver,
                metadataPackagePathResolver,
                unrecognizedPackagePathResolver
        );
    }
}
