package com.github.blindpirate.gogradle.util;

import com.github.blindpirate.gogradle.core.cache.git.GitInteractionException;
import com.github.blindpirate.gogradle.core.dependency.resolve.DependencyResolutionException;
import com.google.common.base.Optional;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    public Set<String> getRemoteUrl(Repository repository) {
        Config config = repository.getConfig();
        Set<String> remotes = config.getSubsections("remote");
        Set<String> ret = new HashSet<>();
        for (String remoteName : remotes) {
            ret.add(config.getString("remote", remoteName, "url"));
        }
        return ret;
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

    public String headCommitOfBranch(Repository repository, String branch) {
        try {
            Ref headRef = repository.exactRef("refs/heads/" + branch);
            return headRef.getObjectId().getName();
        } catch (IOException e) {
            throw new GitInteractionException("Cannot resolve HEAD of " + repository + ":" + branch);
        }
    }

    public Optional<String> findCommit(Repository repository, String commit) {
        try {
            RevWalk walk = new RevWalk(repository);
            ObjectId id = repository.resolve(commit);
            if (id == null) {
                return Optional.absent();
            }
            RevCommit rev = walk.parseCommit(id);

            return Optional.of(rev.getId().toString());
        } catch (IOException e) {
            return Optional.absent();
        }
    }

    public Optional<String> findCommitByTag(Repository repository, String tag) {
        Map<String, Ref> refMap = repository.getTags();
        Ref ref = refMap.get(tag);
        if (ref == null) {
            return Optional.absent();
        } else {
            return Optional.of(ref.getObjectId().getName());
        }
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

}
