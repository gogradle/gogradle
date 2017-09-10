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

package com.github.blindpirate.gogradle.vcs.git;

import com.github.blindpirate.gogradle.util.Assert;
import com.github.blindpirate.gogradle.util.DateUtils;
import com.github.blindpirate.gogradle.util.IOUtils;
import com.github.blindpirate.gogradle.util.ProcessUtils;
import com.github.blindpirate.gogradle.util.StringUtils;
import com.github.blindpirate.gogradle.vcs.GitMercurialAccessor;
import com.github.blindpirate.gogradle.vcs.GitMercurialCommit;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.blindpirate.gogradle.util.DateUtils.toMilliseconds;
import static com.github.blindpirate.gogradle.util.ProcessUtils.ProcessResult;
import static com.github.blindpirate.gogradle.util.StringUtils.toUnixString;
import static java.util.Arrays.asList;
import static java.util.Optional.of;

@Singleton
public class GitClientAccessor extends GitMercurialAccessor {
    private static final String MASTER_BRANCH = "master";
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
        run(repoRoot, asList("git", "checkout", "-f", version));
    }

    @Override
    public String getDefaultBranch(File repoRoot) {
        File refsHeads = new File(repoRoot, ".git/refs/heads");
        if (new File(refsHeads, MASTER_BRANCH).exists()) {
            return MASTER_BRANCH;
        }
        List<File> files = IOUtils.safeListFiles(refsHeads).stream()
                .filter(File::isFile)
                .collect(Collectors.toList());

        Assert.isNotEmpty(files, "Cannot found any files in " + StringUtils.toUnixString(refsHeads));
        files.sort(Comparator.comparing(File::lastModified).reversed());
        return files.get(0).getName();
    }


    @Override
    public String getRemoteUrl(File repoRoot) {
        // git config --get remote.origin.url
        return run(repoRoot,
                asList("git", "config", "--get", "remote.origin.url"),
                result -> result.getStdout().trim());
    }

    @Override
    public long lastCommitTimeOfPath(File repoRoot, Path relativePath) {
        return run(repoRoot,
                asList("git", "log", "-1", "--pretty=format:%ct", toUnixString(relativePath)),
                result -> toMilliseconds(Long.valueOf(result.getStdout().trim())));
    }

    @Override
    public Optional<GitMercurialCommit> findCommitByTagOrBranch(File repository, String tag) {
        return findCommitTagOrBranch(repository, tag);
    }

    private Optional<GitMercurialCommit> findCommitTagOrBranch(File repoRoot, String commitTagOrBranch) {
        return run(repoRoot,
                asList("git", "log", commitTagOrBranch, "-1", "--pretty=format:%H:%ct"),
                result -> {
                    String[] commitAndTime = result.getStdout().split(":");
                    String commit = commitAndTime[0];
                    Long unixSecond = Long.valueOf(commitAndTime[1]);
                    return of(GitMercurialCommit.of(commit, DateUtils.toMilliseconds(unixSecond)));
                },
                result -> Optional.empty()
        );
    }

    @SuppressWarnings("checkstyle:linelength")
    @Override
    public List<GitMercurialCommit> getAllTags(File repository) {
        // git for-each-ref --sort=-committerdate --format '%(objectname):%(refname:short):%(committerdate:iso)' refs/tags
        // 5ddaee09d704261aa360068cdcafff1e5f188ece:v3.4.1:1481274259 +0800
        return run(repository,
                asList("git", "for-each-ref", "--sort=-creatordate", "--format", "%(objectname):%(refname:short):%(creatordate:raw)", "refs/tags"),
                this::convertToCommits);
    }

    private List<GitMercurialCommit> convertToCommits(ProcessResult result) {
        return Stream.of(result.getStdout().split("\\n"))
                .filter(StringUtils::isNotBlank)
                .map(line -> {
                    String[] commitTagAndTime = line.split(":");
                    String commit = commitTagAndTime[0];
                    String tag = commitTagAndTime[1];
                    long ms = DateUtils.parseRaw(commitTagAndTime[2]);
                    return GitMercurialCommit.of(commit, tag, ms);
                }).collect(Collectors.toList());
    }

    @Override
    public Optional<GitMercurialCommit> findCommit(File repository, String commit) {
        return findCommitTagOrBranch(repository, commit);
    }

    @Override
    public GitMercurialCommit headCommitOfBranch(File repository, String branch) {
        return run(repository,
                asList("git", "log", branch, "-1", "--pretty=format:%H:%ct"),
                result -> {
                    String[] commitAndTime = result.getStdout().split(":");
                    String commit = commitAndTime[0];
                    Long unixSecond = Long.valueOf(commitAndTime[1]);
                    return GitMercurialCommit.of(commit, DateUtils.toMilliseconds(unixSecond));
                }
        );
    }

    @Override
    public void update(File repoRoot) {
        runWithProgress(repoRoot,
                asList("git", "pull", "--all", "--progress"),
                GitClientLineConsumer.NO_OP,
                GitClientLineConsumer.of("Updating " + repoRoot.getAbsolutePath()));
        run(repoRoot, asList("git", "submodule", "update", "--init", "--recursive"));

        checkout(repoRoot, getDefaultBranch(repoRoot));
    }

    @Override
    @SuppressWarnings("checkstyle:linelenght")
    public void clone(String url, File directory) {
        runWithProgress(asList("git", "clone", "--recursive", "--progress", url, directory.getAbsolutePath()),
                GitClientLineConsumer.NO_OP,
                GitClientLineConsumer.of("Cloning " + url + " to " + directory.getAbsolutePath()));
    }
}
