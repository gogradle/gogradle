package com.github.blindpirate.gogradle.vcs.mercurial;

import com.github.blindpirate.gogradle.core.cache.GlobalCacheManager;
import com.github.blindpirate.gogradle.core.dependency.DependencyRegistry;
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet;
import com.github.blindpirate.gogradle.core.dependency.NotationDependency;
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency;
import com.github.blindpirate.gogradle.core.dependency.VendorResolvedDependency;
import com.github.blindpirate.gogradle.core.dependency.produce.DependencyVisitor;
import com.github.blindpirate.gogradle.core.dependency.resolve.AbstractVcsDependencyManager;
import com.github.blindpirate.gogradle.core.exceptions.DependencyResolutionException;
import com.github.blindpirate.gogradle.util.Assert;
import com.github.blindpirate.gogradle.util.IOUtils;
import com.github.blindpirate.gogradle.util.ProcessUtils;
import com.github.blindpirate.gogradle.util.StringUtils;
import com.github.blindpirate.gogradle.vcs.GitMercurialNotationDependency;
import com.github.blindpirate.gogradle.vcs.GitMercurialResolvedDependency;
import com.github.blindpirate.gogradle.vcs.mercurial.client.HgClientMercurialAccessor;
import com.github.blindpirate.gogradle.vcs.mercurial.hg4j.Hg4JMercurialAccessor;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

@Singleton
public class MercurialDependencyManager extends AbstractVcsDependencyManager<HgRepository, HgChangeset> {
    public static final String DEFAULT_BRANCH = "default";
    private static final Logger LOGGER = Logging.getLogger(MercurialDependencyManager.class);

    private final MercurialAccessor accessor;

    private final DependencyVisitor visitor;

    @Inject
    public MercurialDependencyManager(HgClientMercurialAccessor hgClientAccessor,
                                      Hg4JMercurialAccessor hg4JAccessor,
                                      DependencyVisitor visitor,
                                      GlobalCacheManager cacheManager,
                                      DependencyRegistry dependencyRegistry) {
        super(cacheManager, dependencyRegistry);
        this.visitor = visitor;
        this.accessor = determineAccessor(hg4JAccessor, hgClientAccessor);
    }

    private MercurialAccessor determineAccessor(Hg4JMercurialAccessor hg4JAccessor,
                                                HgClientMercurialAccessor hgClientAccessor) {
        try {
            Process process = ProcessUtils.run("hg", "version");
            Assert.isTrue(ProcessUtils.getStdout(process).contains("Mercurial"),
                    "Can't find hg in $PATH, do you have mercurial client installed?");
            return hgClientAccessor;
        } catch (Exception e) {
            LOGGER.info("exception in hg version: {}, use hg4j", e);
            return hg4JAccessor;
        }
    }

    @Override
    protected void doReset(ResolvedDependency dependency, Path globalCachePath) {
        GitMercurialResolvedDependency resolvedDependency = (GitMercurialResolvedDependency) dependency;
        HgRepository repository = accessor.getRepository(globalCachePath.toFile());
        accessor.resetToSpecificNodeId(repository, resolvedDependency.getVersion());
    }

    @Override
    protected ResolvedDependency createResolvedDependency(NotationDependency dependency,
                                                          File directory,
                                                          HgRepository hgRepository,
                                                          HgChangeset hgChangeset) {
        GitMercurialNotationDependency notationDependency = GitMercurialNotationDependency.class.cast(dependency);
        GitMercurialResolvedDependency ret = GitMercurialResolvedDependency.mercurialBuilder()
                .withNotationDependency(notationDependency)
                .withName(dependency.getPackage().getRootPath())
                .withCommitId(hgChangeset.getId())
                .withRepoUrl(accessor.getRemoteUrl(hgRepository))
                .withTag(notationDependency.getTag())
                .withCommitTime(hgChangeset.getCommitTime())
                .build();
        GolangDependencySet dependencies = dependency.getStrategy().produce(ret, directory, visitor);
        ret.setDependencies(dependencies);

        setVendorUpdateTimeIfNecessary(hgRepository, dependencies);
        return ret;
    }

    private void setVendorUpdateTimeIfNecessary(HgRepository repository, GolangDependencySet dependencies) {
        dependencies.flatten().stream()
                .filter(dependency -> dependency instanceof VendorResolvedDependency)
                .map(dependency -> (VendorResolvedDependency) dependency)
                .forEach(dependency -> {
                    String relativePath = StringUtils.toUnixString(dependency.getRelativePathToHost());
                    dependency.setUpdateTime(accessor.getLastCommitTimeOfPath(repository, relativePath));
                });
    }

    @Override
    protected void resetToSpecificVersion(HgRepository hgRepository, HgChangeset hgChangeset) {
        accessor.resetToSpecificNodeId(hgRepository, hgChangeset.getId());
    }

    @Override
    protected HgChangeset determineVersion(HgRepository repository, NotationDependency dependency) {
        GitMercurialNotationDependency notationDependency = (GitMercurialNotationDependency) dependency;
        if (notationDependency.getTag() != null) {
            String tag = notationDependency.getTag();
            Optional<HgChangeset> changeset = accessor.findChangesetByTag(repository, tag);
            if (changeset.isPresent()) {
                return changeset.get();
            }
        }
        if (isConcreteCommit(notationDependency.getCommit())) {
            String nodeId = notationDependency.getCommit();
            Optional<HgChangeset> changeset = accessor.findChangesetById(repository, nodeId);
            if (changeset.isPresent()) {
                return changeset.get();
            }
        }

        return accessor.headOfBranch(repository, DEFAULT_BRANCH);
    }

    private boolean isConcreteCommit(String nodeId) {
        return nodeId != null && !GitMercurialNotationDependency.NEWEST_COMMIT.equals(nodeId);
    }

    @Override
    protected HgRepository updateRepository(NotationDependency dependency, HgRepository hgRepository, File directory) {
        accessor.pull(hgRepository);
        return hgRepository;
    }

    @Override
    protected HgRepository initRepository(NotationDependency dependency, File directory) {
        List<String> urls = GitMercurialNotationDependency.class.cast(dependency).getUrls();
        Assert.isNotEmpty(urls, "Urls cannot be empty!");
        for (int i = 0; i < urls.size(); ++i) {
            IOUtils.clearDirectory(directory);
            String url = urls.get(i);
            try {
                return accessor.cloneWithUrl(directory, url);
            } catch (Throwable e) {
                LOGGER.quiet("Cloning with url {} failed, the cause is {}", url, e.getMessage());
                if (i == urls.size() - 1) {
                    throw DependencyResolutionException.cannotCloneRepository(dependency, e);
                }
            }
        }
        throw new IllegalStateException("Urls is empty:" + dependency);
    }

    @Override
    protected Optional<HgRepository> repositoryMatch(File directory, NotationDependency dependency) {
        HgRepository repository = accessor.getRepository(directory);
        String url = accessor.getRemoteUrl(directory);
        if (GitMercurialNotationDependency.class.cast(dependency).getUrls().contains(url)) {
            return Optional.of(repository);
        } else {
            return Optional.empty();
        }
    }
}
