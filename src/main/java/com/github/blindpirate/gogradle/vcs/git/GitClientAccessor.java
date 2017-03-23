package com.github.blindpirate.gogradle.vcs.git;

import com.github.blindpirate.gogradle.util.Assert;
import com.github.blindpirate.gogradle.util.DateUtils;
import com.github.blindpirate.gogradle.util.ProcessUtils;
import com.github.blindpirate.gogradle.vcs.GitMercurialAccessor;
import com.github.blindpirate.gogradle.vcs.GitMercurialCommit;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.blindpirate.gogradle.util.DateUtils.toMilliseconds;
import static com.github.blindpirate.gogradle.util.ProcessUtils.ProcessResult;
import static java.util.Arrays.asList;
import static java.util.Optional.of;

@Singleton
public class GitClientAccessor extends GitMercurialAccessor {
    private Boolean gitClientExists;

    @Inject
    public GitClientAccessor(ProcessUtils processUtils) {
        super(processUtils);
    }

    @Override
    protected void ensureClientExists() {
        if (gitClientExists == null) {
            try {
                processUtils.runAndGetStdout("git", "version");
                gitClientExists = true;
            } catch (Exception e) {
                gitClientExists = false;
            }
        }
        Assert.isTrue(gitClientExists, "Git not found, does it exist in $PATH?");
    }

    @Override
    public void checkout(File repoRoot, String version) {
        run(repoRoot, asList("git", "checkout", version));
    }


    @Override
    public String getRemoteUrl(File repoRoot) {
        // git config --get remote.origin.url
        return run(repoRoot,
                asList("git", "config", "--get", "remote.origin.url"),
                result -> result.getStdout().trim());
    }

    @Override
    public long lastCommitTimeOfPath(File repoRoot, String relativePath) {
        return run(repoRoot,
                asList("git", "log", "-1", "--pretty=format:\"%ct\""),
                result -> toMilliseconds(Long.valueOf(result.getStdout().trim())));
    }

    @Override
    public Optional<GitMercurialCommit> findCommitByTag(File repository, String tag) {
        return findCommitOrTag(repository, tag);
    }

    private Optional<GitMercurialCommit> findCommitOrTag(File repoRoot, String tagOrCommit) {
        return run(repoRoot,
                asList("git", "log", tagOrCommit, "-1", "--pretty=format:\"%H:%ct\""),
                result -> {
                    String[] commitAndTime = result.getStdout().split(":");
                    String commit = commitAndTime[0];
                    Long unixSecond = Long.valueOf(commitAndTime[1]);
                    return of(GitMercurialCommit.of(commit, DateUtils.toMilliseconds(unixSecond)));
                },
                result -> Optional.empty()
        );
    }

    @Override
    public List<GitMercurialCommit> getAllTags(File repository) {
        // git for-each-ref --sort=-committerdate --format '%(objectname):%(refname:short):%(committerdate:iso)' refs/tags
        // 5ddaee09d704261aa360068cdcafff1e5f188ece:v3.4.1:2017-03-03 14:45:52 -0500
        return run(repository,
                asList("git", "for-each-ref", "--sort=-committerdate", "--format", "'%(objectname):%(refname:short):%(committerdate:iso)'", "refs/tags"),
                this::convertToCommits);
    }

    private List<GitMercurialCommit> convertToCommits(ProcessResult result) {
        return Stream.of(result.getStdout().split("\\n")).map(line -> {
            String[] commitTagAndISO8601 = line.split(":");
            String commit = commitTagAndISO8601[0];
            String tag = commitTagAndISO8601[1];
            long ms = Instant.parse(commitTagAndISO8601[2]).toEpochMilli();
            return GitMercurialCommit.of(commit, tag, ms);
        }).collect(Collectors.toList());
    }

    @Override
    public Optional<GitMercurialCommit> findCommit(File repository, String commit) {
        return findCommitOrTag(repository, commit);
    }

    @Override
    public GitMercurialCommit headCommitOfBranch(File repository, String branch) {
        return run(repository,
                asList("git", "log", "-1", "--pretty=format:\"%H:%ct\""),
                result -> {
                    String[] commitAndTime = result.getStdout().split(":");
                    String commit = commitAndTime[0];
                    Long unixSecond = Long.valueOf(commitAndTime[1]);
                    return GitMercurialCommit.of(commit, DateUtils.toMilliseconds(unixSecond));
                }
        );
    }

    @Override
    public void hardResetAndPull(File repoRoot, Map<String, String> proxyEnv) {
        run(repoRoot, asList("git", "pull"), proxyEnv);
    }

    @Override
    public void clone(String url, File directory, Map<String, String> proxyEnv) {
        run(null, asList("git", "clone", url, directory.getAbsolutePath()), proxyEnv);
    }
}
