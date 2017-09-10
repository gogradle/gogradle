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

package com.github.blindpirate.gogradle.vcs.mercurial;

import com.github.blindpirate.gogradle.util.Assert;
import com.github.blindpirate.gogradle.util.DateUtils;
import com.github.blindpirate.gogradle.util.ProcessUtils;
import com.github.blindpirate.gogradle.vcs.GitMercurialAccessor;
import com.github.blindpirate.gogradle.vcs.GitMercurialCommit;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.blindpirate.gogradle.util.StringUtils.toUnixString;
import static com.github.blindpirate.gogradle.util.StringUtils.trimToNull;
import static java.util.Arrays.asList;

@Singleton
public class HgClientAccessor extends GitMercurialAccessor {
    private static final String DEFAULT_BRANCH = "default";
    private static final Pattern DEFAULT_URL_PATTERN = Pattern.compile("default\\s*=\\s*(\\S+)");
    // tip                                2:620889544e2d
    // commit2_tag                        1:1eaebd519f4c
    private static final Pattern TAGS_PATTERN = Pattern.compile("(\\w+)\\s+(\\d)+:([a-fA-F0-9]+)");
    private Boolean hgClientExists;

    @Inject
    public HgClientAccessor(ProcessUtils processUtils) {
        super(processUtils);
    }

    @Override
    protected void ensureClientExists() {
        if (hgClientExists == null) {
            try {
                processUtils.runAndGetStdout("hg", "version");
                hgClientExists = true;
            } catch (Exception e) {
                hgClientExists = false;
            }
        }
        Assert.isTrue(hgClientExists, "Mercurial not found, does it exist in $PATH?");
    }

    @Override
    public void checkout(File repoRoot, String version) {
        run(repoRoot, asList("hg", "checkout", version, "--clean"));
    }

    @Override
    public String getDefaultBranch(File repoRoot) {
        return DEFAULT_BRANCH;
    }

    @Override
    public String getRemoteUrl(File repoRoot) {
        return run(repoRoot, asList("hg", "path"), result -> {
            Matcher matcher = DEFAULT_URL_PATTERN.matcher(result.getStdout());
            Assert.isTrue(matcher.find(), "Cannot found url in hg paths output: " + result.getStdout());
            return matcher.group(1);
        });
    }

    @Override
    public long lastCommitTimeOfPath(File repoRoot, Path relativePath) {
        return run(repoRoot,
                asList("hg", "log", toUnixString(relativePath), "--limit", "1", "--template", "{date|hgdate}"),
                result -> DateUtils.parseRaw(result.getStdout())
        );
    }


    @Override
    public Optional<GitMercurialCommit> findCommitByTagOrBranch(File repository, String tag) {
        return findCommitByTagOrCommit(repository, tag);
    }

    private Optional<GitMercurialCommit> findCommitByTagOrCommit(File repoRoot, String tagOrCommit) {
        // TODO
        // {tags} may be referred to multiple tags
        return run(repoRoot,
                asList("hg", "log", "-r", tagOrCommit, "--limit", "1", "--template", "{node}:{tags}:{date|hgdate}"),
                result -> Optional.of(extractOne(result.getStdout())),
                result -> Optional.empty());
    }

    private GitMercurialCommit extractOne(String line) {
        // 6e7786086f774483c392827fd358523ebbf7bb5c:v1.2m2:1375988692 -7200
        String[] commitTagAndTime = line.split(":");
        Assert.isTrue(commitTagAndTime.length == 3, "Unrecognized line: " + line);

        String id = commitTagAndTime[0];
        String tag = trimToNull(commitTagAndTime[1]);
        long time = DateUtils.parseRaw(commitTagAndTime[2]);

        return GitMercurialCommit.of(id, tag, time);
    }

    @Override
    public List<GitMercurialCommit> getAllTags(File repository) {
        return run(repository,
                asList("hg", "log", "--rev=tag()", "--template", "{node}:{tags}:{date|hgdate}\\n"),
                result ->
                        Stream.of(result.getStdout().split("\\n"))
                                .map(this::extractOne)
                                .collect(Collectors.toList())
        );

    }

    @Override
    public Optional<GitMercurialCommit> findCommit(File repository, String commit) {
        return findCommitByTagOrCommit(repository, commit);
    }

    @Override
    public GitMercurialCommit headCommitOfBranch(File repository, String branch) {
        return run(repository,
                asList("hg", "log", "--limit", "1", "--template", "{node}:{tags}:{date|hgdate}"),
                result -> extractOne(result.getStdout())
        );
    }

    @Override
    public void update(File repoRoot) {
        run(repoRoot, asList("hg", "pull", "-u"));
    }

    @Override
    public void clone(String url, File directory) {
        run(new File("."), asList("hg", "clone", url, directory.getAbsolutePath()));
    }
}
