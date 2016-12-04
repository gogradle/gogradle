package com.github.blindpirate.gogradle.core.cache.git;

import com.github.blindpirate.gogradle.GolangPluginSetting;
import com.github.blindpirate.gogradle.core.TempFileModule;
import com.github.blindpirate.gogradle.core.cache.CacheManager;
import com.github.blindpirate.gogradle.core.dependency.GitDependency;
import com.github.blindpirate.gogradle.core.dependency.resolve.DependencyResolutionException;
import com.github.blindpirate.gogradle.util.GitUtils;
import com.google.common.base.Optional;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.github.blindpirate.gogradle.util.FileUtils.ensureExistAndWritable;

public class GitReader {

    @Inject
    private GolangPluginSetting setting;
    @Inject
    private CacheManager cacheManager;

    public Object getMetadata(GitDependency gitDependency) {
        Repository repository = prepareForGlobalCacheRepository(gitDependency);
        findOutSatisfiedCommit(repository, gitDependency);
        return null;
    }

    private Object findOutSatisfiedCommit(Repository repository, GitDependency gitDependency) {
        if (gitDependency.getSemVersion() != null) {
            Optional<Object> commit = findBySemVersion(repository, gitDependency);
            if (commit.isPresent()) {
                return commit.get();
            }
        }
        if (gitDependency.getTag() != null) {
            Optional<Object> commit = findByTag(repository, gitDependency.getTag());
            if (commit.isPresent()) {
                return commit.get();
            }
        }

        if (gitDependency.getCommit() != null) {
            Optional commit = findByCommit(repository, gitDependency.getCommit());
            return commit;
        }
        return null;
    }

    private Optional findByCommit(Repository repository, String commit) {
        try {
            RevWalk walk = new RevWalk(repository);
            ObjectId id = repository.resolve(commit);
            RevCommit rev = walk.parseCommit(id);

            return Optional.of(rev.getId());

        } catch (IOException e) {
            return Optional.absent();
        }
    }

    private Optional findByTag(Repository repository, String tag) {
        Ref ref = repository.getTags().get(tag);
        if (ref == null) {
            return Optional.absent();
        } else {
            return Optional.of(ref.getObjectId());
        }
    }

    private Optional<Object> findBySemVersion(Repository repository, GitDependency gitDependency) {
//        Map<String, Ref> tags = repository.getTags();

//        List<Version> satisfiedVerson = new ArrayList<>();

        // TODO find the biggest version
//        for (String tag : tags.keySet()) {
//            Version tagVersion = Version.valueOf(tag);
//            if (tagVersion.satisfies(tag)) {
////                satisfiedVerson.add(tagVersion);
//                return (Optional) Optional.of(tags.get(tag).getObjectId());
//            }
//        }
        // See
        // https://github.com/centic9/jgit-cookbook/blob/master/src/main/java/org/dstadler/jgit/porcelain/ListTags.java

        return Optional.absent();
    }


    private Repository prepareForGlobalCacheRepository(GitDependency gitDependency) {
        Path cacheDirectoryPath = getGlobalCachePath(gitDependency);
        ensureExistAndWritable(cacheDirectoryPath);

        Optional<Repository> globalCache = globalCacheExist(gitDependency);
        if (!globalCache.isPresent()) {
            return initGlobalCache(gitDependency);
        } else {
            return makeRepositoryUpToDate(globalCache.get());
        }

    }

    private Path getGlobalCachePath(GitDependency gitDependency) {
        return cacheManager.getGlobalCachePath(gitDependency.getName());
    }

    private Repository initGlobalCache(GitDependency gitDependency) {
        Optional<Repository> packageInGlobalGopath = sameGitRepositoryInGlobalGopath(gitDependency);
        Repository globalCache;
        if (packageInGlobalGopath.isPresent()) {
            globalCache = copyFromGlobalGopath(gitDependency);
            makeRepositoryUpToDate(globalCache);
        } else {
            globalCache = cloneFromRemoteToGlobalCache(gitDependency);
        }
        return globalCache;
    }

    private Repository cloneFromRemoteToGlobalCache(GitDependency gitDependency) {
        try {
            Path path = getGlobalCachePath(gitDependency);
            Git git = Git.cloneRepository()
                    .setURI(gitDependency.getUrl())
                    .setDirectory(path.toFile())
                    .call();
            return git.getRepository();
        } catch (GitAPIException e) {
            throw new GitInteractionException("Exception in git operation", e);
        }
    }

    private Repository copyFromGlobalGopath(GitDependency gitDependency) {
        Path packageInGlobalGopath = getPathInGlobalGopath(gitDependency);
        Path globalCachePath = getGlobalCachePath(gitDependency);
        try {
            Files.copy(packageInGlobalGopath, globalCachePath);
            return getRepository(globalCachePath);
        } catch (IOException e) {
            throw new DependencyResolutionException(e);
        }
    }

    private Path getPathInGlobalGopath(GitDependency gitDependency) {
        Path gopath = Paths.get(setting.getGlobalGopath());

        Path repoPath = gopath.resolve(gitDependency.getName());

        return repoPath;
    }

    private Repository makeRepositoryUpToDate(Repository repository) {
        try {
            Git git = Git.wrap(repository);
            git.reset().setMode(ResetCommand.ResetType.HARD).call();
            git.pull().call();
            return repository;
        } catch (GitAPIException e) {
            throw new GitInteractionException("Exception in git operation", e);
        }
    }

    private Optional<Repository> globalCacheExist(GitDependency gitDependency) {
        Path repoPath = getGlobalCachePath(gitDependency);
        return sameGitRepositoryExist(repoPath, gitDependency);
    }


    private Optional<Repository> sameGitRepositoryInGlobalGopath(GitDependency gitDependency) {
        if (setting.getGlobalGopath() == null) {
            return Optional.absent();
        }
        return sameGitRepositoryExist(getGlobalCachePath(gitDependency), gitDependency);
    }

    private Optional<Repository> sameGitRepositoryExist(Path repoPath, GitDependency gitDependency) {
        try {
            Repository repository = getRepository(repoPath);

            // TODO git@github.com:a/b.git and https://github.com/a/b.git
            if (GitUtils.getRemoteUrl(repository).contains(gitDependency.getUrl())) {
                return Optional.of(repository);
            } else {
                return Optional.absent();
            }
        } catch (IOException e) {
            return Optional.absent();
        }
    }

    private Repository getRepository(Path repoPath) throws IOException {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();

        return builder.setGitDir(repoPath.toFile())
                .readEnvironment()
                .findGitDir()
                .build();
    }

    public TempFileModule resolve(GitDependency gitDependency) {
        return null;
    }
}
