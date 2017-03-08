package com.github.blindpirate.gogradle.vcs.mercurial.client;

import com.github.blindpirate.gogradle.util.Assert;
import com.github.blindpirate.gogradle.util.ProcessUtils;
import com.github.blindpirate.gogradle.util.StringUtils;
import com.github.blindpirate.gogradle.vcs.mercurial.HgChangeset;
import com.github.blindpirate.gogradle.vcs.mercurial.HgRepository;
import com.github.blindpirate.gogradle.vcs.mercurial.MercurialAccessor;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.blindpirate.gogradle.util.DateUtils.toMilliseconds;
import static com.github.blindpirate.gogradle.util.ProcessUtils.ProcessResult;
import static com.github.blindpirate.gogradle.util.ProcessUtils.run;
import static java.util.Arrays.asList;

public class HgClientMercurialAccessor implements MercurialAccessor {
    private static final Pattern DEFAULT_URL_PATTERN = Pattern.compile("default\\s*=\\s*(\\S+)");
    // tip                                2:620889544e2d
    // commit2_tag                        1:1eaebd519f4c
    private static final Pattern TAGS_PATTERN = Pattern.compile("(\\w+)\\s+(\\d)+:([a-fA-F0-9]+)");

    @Override
    public String getRemoteUrl(File repoRoot) {
        Process process = run(asList("hg", "paths"), null, repoRoot);
        ProcessResult result = ProcessUtils.getResult(process);

        checkReturnCode(result, asList("hg", "paths"), repoRoot);

        Matcher matcher = DEFAULT_URL_PATTERN.matcher(result.getStdout());
        Assert.isTrue(matcher.find(), "Cannot found url in hg paths output: " + result.getStdout());

        return matcher.group(1);
    }

    @Override
    public HgRepository getRepository(File dir) {
        return HgClientRepository.of(dir);
    }

    @Override
    public String getRemoteUrl(HgRepository repository) {
        return getRemoteUrl(HgClientRepository.class.cast(repository).getRootDir());
    }

    @Override
    public void resetToSpecificNodeId(HgRepository repository, String version) {
        runCmdsInRepo(asList("hg", "checkout", version, "--clean"), repository);
    }

    private void runCmdsInRepo(List<String> cmds, HgRepository repository) {
        File repoRoot = repository == null ? null : HgClientRepository.class.cast(repository).getRootDir();
        Process process = run(cmds, null, repoRoot);
        checkReturnCode(ProcessUtils.getResult(process), cmds, repoRoot);
    }

    @Override
    public long getLastCommitTimeOfPath(HgRepository repository, String relativePath) {
        File repoRoot = HgClientRepository.class.cast(repository).getRootDir();
        List<String> cmds = asList("hg", "log", relativePath, "--limit", "1", "--template", "{date|hgdate}");
        Process process = run(cmds, null, repoRoot);
        ProcessResult result = ProcessUtils.getResult(process);

        checkReturnCode(result, cmds, repoRoot);

        return extractCommitTimeInMillisecond(result.getStdout(), cmds);
    }

    private long extractCommitTimeInMillisecond(String stdout, List<String> cmds) {
        String[] timestampAndTimezone = StringUtils.splitAndTrim(stdout, "\\s");

        Assert.isTrue(timestampAndTimezone.length == 2, "Invalid output of " + cmds + ":" + stdout);
        long commitEpochTime = Long.parseLong(timestampAndTimezone[0]);
        return commitEpochTime * 1000;
    }

    private void checkReturnCode(ProcessResult result, List<String> cmds, File repo) {
        Assert.isTrue(result.getCode() == 0,
                String.format("Return code of %s in %s is %d", cmds, repo, result.getCode()));
    }

    @Override
    public Optional<HgChangeset> findChangesetByTag(HgRepository repository, String tag) {
        File repoRoot = HgClientRepository.class.cast(repository).getRootDir();
        Process process = run(asList("hg", "tags", "--debug"), null, repoRoot);
        ProcessResult result = ProcessUtils.getResult(process);

        checkReturnCode(result, asList("hg", "tags", "--debug"), repoRoot);

        return extractChangesetIdFromHgTags(result.getStdout(), tag)
                .map(changesetId -> findDetailOfChangeset(changesetId, repoRoot));
    }

    private Optional<String> extractChangesetIdFromHgTags(String output, String targetTag) {
        Matcher matcher = TAGS_PATTERN.matcher(output);
        while (matcher.find()) {
            String tag = matcher.group(1);
            if (tag.equals(targetTag)) {
                return Optional.of(matcher.group(3));
            }
        }
        return Optional.empty();
    }

    private HgChangeset findDetailOfChangeset(String changesetId, File repo) {
        // 1487252847 -28800
        List<String> cmds = Arrays.asList("hg", "log", "-r" + changesetId, "--template", "{date|hgdate}");
        Process process = run(cmds, null, repo);
        ProcessResult result = ProcessUtils.getResult(process);

        checkReturnCode(result, cmds, repo);

        return HgClientChangeset.of(changesetId, extractCommitTimeInMillisecond(result.getStdout(), cmds));
    }

    @Override
    public Optional<HgChangeset> findChangesetById(HgRepository repository, String id) {
        File repoRoot = HgClientRepository.class.cast(repository).getRootDir();
        return Optional.ofNullable(findDetailOfChangeset(id, repoRoot));
    }

    @Override
    public HgChangeset headOfBranch(HgRepository repository, String defaultBranch) {
        File repoRoot = HgClientRepository.class.cast(repository).getRootDir();
        // 620889544e2db8b064180431bcd1bb965704f4c2 1487252847 -28800
        List<String> cmds = asList("hg", "log", "--limit", "1", "--template", "{node} {date|hgdate}");

        Process process = run(cmds, null, repoRoot);
        ProcessResult result = ProcessUtils.getResult(process);
        checkReturnCode(result, cmds, repoRoot);

        String[] idTimestampTimezone = StringUtils.splitAndTrim(result.getStdout(), "\\s");
        Assert.isTrue(idTimestampTimezone.length == 3, "Invalid output of " + cmds + ":" + result.getStdout());

        long commitEpochTime = Long.parseLong(idTimestampTimezone[1]);
        return HgClientChangeset.of(idTimestampTimezone[0], toMilliseconds(commitEpochTime));
    }

    @Override
    public void pull(HgRepository repository) {
        runCmdsInRepo(asList("hg", "pull", "-u"), repository);
    }

    @Override
    public HgRepository cloneWithUrl(File directory, String url) {
        runCmdsInRepo(asList("hg", "clone", url, directory.getAbsolutePath()), null);
        return getRepository(directory);
    }
}
