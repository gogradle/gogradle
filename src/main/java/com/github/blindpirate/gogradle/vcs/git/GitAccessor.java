package com.github.blindpirate.gogradle.vcs.git;

import com.github.blindpirate.gogradle.GitRepositoryHandler;
import com.github.blindpirate.gogradle.GogradleGlobal;
import com.github.blindpirate.gogradle.core.dependency.resolve.LoggerProgressMonitor;
import com.github.blindpirate.gogradle.util.Assert;
import com.github.blindpirate.gogradle.util.DateUtils;
import com.github.blindpirate.gogradle.util.ExceptionHandler;
import com.github.blindpirate.gogradle.vcs.VcsAccessor;
import com.github.zafarkhaja.semver.ParseException;
import com.github.zafarkhaja.semver.UnexpectedCharacterException;
import com.github.zafarkhaja.semver.Version;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.SubmoduleInitCommand;
import org.eclipse.jgit.api.SubmoduleUpdateCommand;
import org.eclipse.jgit.api.TransportCommand;
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
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.github.blindpirate.gogradle.util.IOUtils.dirIsEmpty;

@Singleton
public class GitAccessor implements VcsAccessor {
    private static final Logger LOGGER = Logging.getLogger(GitAccessor.class);

    private final GitRepositoryHandler gitRepositoryHandler;

    @Inject
    public GitAccessor(GitRepositoryHandler gitRepositoryHandler) {
        this.gitRepositoryHandler = gitRepositoryHandler;
    }

    @Override
    public String getRemoteUrl(File directory) {
        Repository repository = getRepository(directory);
        return getRemoteUrl(repository);
    }

    public Repository getRepository(File directory) {
        try {
            FileRepositoryBuilder builder = new FileRepositoryBuilder();

            return builder
                    .setGitDir(new File(directory, ".git"))
                    .readEnvironment()
                    .findGitDir()
                    .build();
        } catch (IOException e) {
            throw new IllegalStateException("Cannot get repository from path:" + directory.getAbsolutePath());
        }
    }

    public String getRemoteUrl(Repository repository) {
        Config config = repository.getConfig();
        Set<String> remotes = config.getSubsections("remote");
        // Only use the first remote url
        return remotes.stream()
                .map(remoteName -> config.getString("remote", remoteName, "url"))
                .findFirst()
                .get();
    }

    public void cloneWithUrl(String rootPath, String gitUrl, File directory) {
        if (GogradleGlobal.isOffline()) {
            LOGGER.debug("Cloning {} is skipped since it is offline now.", gitUrl);
            return;
        }
        try {
            LOGGER.quiet("Cloing {} into {}", gitUrl, directory);
            LoggerProgressMonitor monitor = new LoggerProgressMonitor("Cloning from " + gitUrl);
            CloneCommand command = Git.cloneRepository()
                    .setURI(gitUrl)
                    .setCloneSubmodules(true)
                    .setProgressMonitor(monitor)
                    .setDirectory(directory);

            setCredentialsIfNecessary(command, rootPath, gitUrl);

            command.call();
            monitor.completed();
        } catch (GitAPIException e) {
            throw new IllegalStateException("Exception in git operation", e);
        }
    }

    private void setCredentialsIfNecessary(TransportCommand<?, ?> command, String name, String url) {
        Optional<GitRepository> matched = gitRepositoryHandler.findMatchedRepository(name, url);
        matched.ifPresent(gitRepository -> gitRepository.configure(command));
    }

    public Optional<RevCommit> headCommitOfBranch(Repository repository, String branch) {
        Assert.isTrue(!dirIsEmpty(repository.getDirectory()),
                "" + repository.getDirectory() + " is empty, are you offline now?");

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

    public Repository hardResetAndPull(String packageRoot, Repository repository) {
        try {
            Git git = Git.wrap(repository);
            // Add all unstaged files and then reset to clear them
            reset(git);

            String url = getRemoteUrl(repository);

            pull(packageRoot, git, url);

            updateSubmodule(packageRoot, repository, url);

            return repository;
        } catch (GitAPIException e) {
            throw new IllegalStateException("Exception in git operation", e);
        }
    }

    private void updateSubmodule(String packageRoot, Repository repository, String url) throws GitAPIException {
        new SubmoduleInitCommand(repository).call();
        SubmoduleUpdateCommand suc = new SubmoduleUpdateCommand(repository)
                .setProgressMonitor(new LoggerProgressMonitor("Pulling from" + url));
        setCredentialsIfNecessary(suc, packageRoot, url);
        suc.call();
    }

    private void pull(String packageRoot, Git git, String url) throws GitAPIException {
        PullCommand pullCommand = git.pull()
                .setProgressMonitor(new LoggerProgressMonitor("Pulling from " + url));
        setCredentialsIfNecessary(pullCommand, packageRoot, url);
        pullCommand.call();
    }

    private void reset(Git git) throws GitAPIException {
        git.add().addFilepattern(".").call();
        git.reset().setMode(ResetCommand.ResetType.HARD).call();
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

    public void checkout(Repository repository, String commitOrBranch) {
        try {
            Git git = Git.wrap(repository);
            git.checkout().setName(commitOrBranch).setForce(true).call();
        } catch (GitAPIException e) {
            throw ExceptionHandler.uncheckException(e);
        }
    }
}
