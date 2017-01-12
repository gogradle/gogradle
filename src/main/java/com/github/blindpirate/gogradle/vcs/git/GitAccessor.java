package com.github.blindpirate.gogradle.vcs.git;

import com.github.blindpirate.gogradle.core.exceptions.DependencyResolutionException;
import com.github.blindpirate.gogradle.util.Assert;
import com.github.blindpirate.gogradle.util.DateUtils;
import com.github.blindpirate.gogradle.util.ExceptionHandler;
import com.github.blindpirate.gogradle.vcs.VcsAccessor;
import com.github.zafarkhaja.semver.ParseException;
import com.github.zafarkhaja.semver.UnexpectedCharacterException;
import com.github.zafarkhaja.semver.Version;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
public class GitAccessor implements VcsAccessor {
    @Override
    public String getRemoteUrl(File directory) {
        Repository repository = getRepository(directory);
        Set<String> urls = getRemoteUrls(repository);
        Assert.isNotEmpty(urls, "Cannot get remote url of git repo " + directory.getAbsolutePath());
        return urls.stream().findFirst().get();
    }

    public Repository getRepository(File directory) {
        try {
            FileRepositoryBuilder builder = new FileRepositoryBuilder();

            return builder
                    .setGitDir(directory.toPath().resolve(".git").toFile())
                    .readEnvironment()
                    .findGitDir()
                    .build();
        } catch (IOException e) {
            throw new IllegalStateException("Cannot get repository from path:" + directory.getAbsolutePath());
        }
    }

    public Set<String> getRemoteUrls(Repository repository) {
        Config config = repository.getConfig();
        Set<String> remotes = config.getSubsections("remote");
        return remotes.stream()
                .map(remoteName -> config.getString("remote", remoteName, "url"))
                .collect(Collectors.toSet());
    }

    public void cloneWithUrl(String gitUrl, File directory) {
        try {
            Git.cloneRepository()
                    .setURI(gitUrl)
                    .setProgressMonitor(new LoggerProgressMonitor())
                    .setDirectory(directory)
                    .call();
        } catch (GitAPIException e) {
            throw new IllegalStateException("Exception in git operation", e);
        }
    }

    public Optional<RevCommit> headCommitOfBranch(Repository repository, String branch) {
        try {
            Ref headRef = repository.exactRef("refs/heads/" + branch);
            String commitId = headRef.getObjectId().name();
            return findCommit(repository, commitId);
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot produce HEAD of " + repository + ":" + branch);
        }
    }

    public Optional<RevCommit> findCommit(Repository repository, String commit) {
        try {
            RevWalk walk = new RevWalk(repository);
            ObjectId id = repository.resolve(commit);
            if (id == null) {
                return Optional.empty();
            }
            RevCommit rev = walk.parseCommit(id);

            return Optional.of(rev);
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    public Optional<RevCommit> findCommitByTag(Repository repository, String tag) {
        Map<String, Ref> refMap = repository.getTags();
        Ref ref = refMap.get(tag);
        if (ref == null) {
            return Optional.empty();
        } else {
            return Optional.of(getCommitByRef(repository, ref));
        }
    }

    private RevCommit getCommitByRef(Repository repository, Ref ref) {
        try (Git git = Git.wrap(repository)) {
            LogCommand log = git.log();
            Ref peeledRef = repository.peel(ref);
            if (peeledRef.getPeeledObjectId() != null) {
                log.add(peeledRef.getPeeledObjectId());
            } else {
                log.add(ref.getObjectId());
            }
            Iterable<RevCommit> commits = log.call();

            for (RevCommit commit : commits) {
                return commit;
            }

            throw new IllegalStateException("Cannot find commit!");
        } catch (GitAPIException | MissingObjectException | IncorrectObjectTypeException e) {
            throw new IllegalStateException(e);
        }
    }

    public void resetToCommit(Repository repository, String commitId) {
        ResetCommand reset = new ResetCommand(repository);
        reset.setMode(ResetCommand.ResetType.HARD);
        reset.setRef(commitId);
        try {
            reset.call();
        } catch (GitAPIException e) {
            throw DependencyResolutionException.cannotResetToCommit(commitId, e);
        }
    }

    public Optional<RevCommit> findCommitBySemVersion(Repository repository, String semVersionExpression) {
        Map<String, Ref> tags = repository.getTags();

        List<Pair<RevCommit, Version>> satisfiedVersion = new ArrayList<>();

        for (Map.Entry<String, Ref> entry : tags.entrySet()) {
            String tag = entry.getKey();

            try {
                Version version = Version.valueOf(tag);
                if (version.satisfies(semVersionExpression)) {
                    RevCommit commit = getCommitByRef(repository, entry.getValue());
                    satisfiedVersion.add(Pair.of(commit, version));
                }
            } catch (UnexpectedCharacterException e) {
                continue;
            } catch (IllegalArgumentException | ParseException e) {
                continue;
            }
        }

        if (satisfiedVersion.isEmpty()) {
            return Optional.empty();
        }

        Collections.sort(satisfiedVersion, (version1, version2) ->
                version2.getRight().compareTo(version1.getRight()));

        return Optional.of(satisfiedVersion.get(0).getLeft());
    }

    public Repository hardResetAndUpdate(Repository repository) {
        try {
            Git git = Git.wrap(repository);
            // Add all unstaged files and then reset to clear them
            git.add().addFilepattern(".").call();
            git.reset().setMode(ResetCommand.ResetType.HARD).call();
            git.pull().call();
            return repository;
        } catch (GitAPIException e) {
            throw new IllegalStateException("Exception in git operation", e);
        }
    }

    public String getRemoteUrl(Repository repository) {
        Set<String> urls = getRemoteUrls(repository);
        Assert.isTrue(!urls.isEmpty(), "Cannot get remote urls of repository:" + repository.getDirectory());
        return urls.stream().findFirst().get();
    }

    public long lastCommitTimeOfPath(Repository repository, String path) {
        try {
            Iterable<RevCommit> logs = new Git(repository).log().addPath(path).call();
            // Can I assume the result is time desc?
            for (RevCommit commit : logs) {
                return DateUtils.toMilliseconds(commit.getCommitTime());
            }
            throw new IllegalStateException("Cannot find " + path + " in repo " + repository
                    + " at commit" + getCurrentCommit(repository).getName() + ", is it force-pushed?");
        } catch (GitAPIException | IOException e) {
            throw ExceptionHandler.uncheckException(e);
        }
    }

    private RevCommit getCurrentCommit(Repository repository) throws IOException, GitAPIException {
        for (RevCommit commit : new Git(repository).log().all().call()) {
            return commit;
        }
        throw new IllegalStateException("A initial repository " + repository + "? You must be kidding me!");
    }
}
