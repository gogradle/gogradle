package com.github.blindpirate.gogradle.util;

import com.github.blindpirate.gogradle.vcs.git.GitInteractionException;
import com.github.blindpirate.gogradle.core.exceptions.DependencyResolutionException;
import com.github.zafarkhaja.semver.ParseException;
import com.github.zafarkhaja.semver.UnexpectedCharacterException;
import com.github.zafarkhaja.semver.Version;
import com.google.common.base.Optional;
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
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.github.blindpirate.gogradle.util.CollectionUtils.*;

@Singleton
public class GitUtils {
    public Repository getRepository(Path path) {
        try {
            FileRepositoryBuilder builder = new FileRepositoryBuilder();

            return builder
                    .setGitDir(path.resolve(".git").toFile())
                    .readEnvironment()
                    .findGitDir()
                    .build();
        } catch (IOException e) {
            throw new IllegalStateException("Cannot get repository from path:" + path.toString());
        }
    }

    public Set<String> getRemoteUrls(Repository repository) {
        Config config = repository.getConfig();
        Set<String> remotes = config.getSubsections("remote");
        Set<String> ret = new HashSet<>();
        for (String remoteName : remotes) {
            ret.add(config.getString("remote", remoteName, "url"));
        }
        return ret;
    }

    public String getRemoteUrl(Repository repository) {
        Optional<String> result = findFirst(getRemoteUrls(repository));
        Assert.isTrue(result.isPresent(), "Cannot find remote url!");
        return result.get();
    }


    public void cloneWithUrl(String gitUrl, Path location) {
        try {
            Git.cloneRepository()
                    .setURI(gitUrl)
                    .setDirectory(location.toFile())
                    .call();
        } catch (GitAPIException e) {
            throw new GitInteractionException("Exception in git operation", e);
        }
    }

    public Optional<RevCommit> headCommitOfBranch(Repository repository, String branch) {
        try {
            Ref headRef = repository.exactRef("refs/heads/" + branch);
            String commitId = headRef.getObjectId().name();
            return findCommit(repository, commitId);
        } catch (IOException e) {
            throw new GitInteractionException("Cannot resolve HEAD of " + repository + ":" + branch);
        }
    }

    public Optional<RevCommit> findCommit(Repository repository, String commit) {
        try {
            RevWalk walk = new RevWalk(repository);
            ObjectId id = repository.resolve(commit);
            if (id == null) {
                return Optional.absent();
            }
            RevCommit rev = walk.parseCommit(id);

            return Optional.of(rev);
        } catch (IOException e) {
            return Optional.absent();
        }
    }

    public Optional<RevCommit> findCommitByTag(Repository repository, String tag) {
        Map<String, Ref> refMap = repository.getTags();
        Ref ref = refMap.get(tag);
        if (ref == null) {
            return Optional.absent();
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

            for (RevCommit commit : log.call()) {
                return commit;
            }
        } catch (GitAPIException | MissingObjectException | IncorrectObjectTypeException e) {
            throw new IllegalStateException(e);
        }
        return null;
    }

    public void resetToCommit(Repository repository, String commitId) {
        ResetCommand reset = new ResetCommand(repository);
        reset.setMode(ResetCommand.ResetType.HARD);
        reset.setRef(commitId);
        try {
            reset.call();
        } catch (GitAPIException e) {
            throw new DependencyResolutionException("Can not reset to specific commit:" + commitId);
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
            return Optional.absent();
        }

        Collections.sort(satisfiedVersion, new Comparator<Pair<RevCommit, Version>>() {
            @Override
            public int compare(Pair<RevCommit, Version> version1, Pair<RevCommit, Version> version2) {
                return version2.getRight().compareTo(version1.getRight());
            }
        });

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
            throw new GitInteractionException("Exception in git operation", e);
        }
    }

}
