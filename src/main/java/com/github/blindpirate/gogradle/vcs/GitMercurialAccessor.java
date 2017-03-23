package com.github.blindpirate.gogradle.vcs;


import com.github.blindpirate.gogradle.core.exceptions.BuildException;
import com.github.blindpirate.gogradle.util.ProcessUtils;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static java.util.Collections.emptyMap;

public abstract class GitMercurialAccessor implements VcsAccessor {

    protected final ProcessUtils processUtils;

    public GitMercurialAccessor(ProcessUtils processUtils) {
        this.processUtils = processUtils;
    }

    public abstract void checkout(File repoRoot, String version);

    @Override
    public abstract String getRemoteUrl(File repoRoot);

    public abstract long lastCommitTimeOfPath(File repoRoot, String relativePath);

    public abstract Optional<GitMercurialCommit> findCommitByTag(File repository, String tag);

    public abstract List<GitMercurialCommit> getAllTags(File repository);

    public abstract Optional<GitMercurialCommit> findCommit(File repository, String commit);

    public abstract GitMercurialCommit headCommitOfBranch(File repository, String branch);

    public abstract void hardResetAndPull(File repoRoot, Map<String, String> proxyEnv);

    public abstract void clone(String url, File directory, Map<String, String> proxyEnv);

    protected void run(File workingDir, List<String> cmds) {
        run(workingDir, cmds, emptyMap());
    }

    protected void run(File workingDir, List<String> cmds, Map<String, String> env) {
        run(workingDir, cmds, env, result -> null, result -> {
            throw BuildException.processInteractionFailed(cmds, env, workingDir, result);
        });
    }

    protected <T> T run(File workingDir, List<String> cmds, Function<ProcessUtils.ProcessResult, T> successFunc) {
        return run(workingDir, cmds, emptyMap(), successFunc, result -> {
            throw BuildException.processInteractionFailed(cmds, emptyMap(), workingDir, result);
        });
    }

    protected <T> T run(File workingDir, List<String> cmds,
                        Function<ProcessUtils.ProcessResult, T> successFunc, Function<ProcessUtils.ProcessResult, T> failureFunc) {
        return run(workingDir, cmds, emptyMap(), successFunc, failureFunc);
    }

    protected <T> T run(File workingDir,
                        List<String> cmds,
                        Map<String, String> env,
                        Function<ProcessUtils.ProcessResult, T> successFunc,
                        Function<ProcessUtils.ProcessResult, T> failFunc) {
        ensureClientExists();

        Process process = processUtils.run(cmds, env, workingDir);

        ProcessUtils.ProcessResult result = processUtils.getResult(process);

        try {
            if (result.getCode() == 0) {
                return successFunc.apply(result);
            } else {
                return failFunc.apply(result);
            }
        } catch (Exception e) {
            throw BuildException.processInteractionFailed(cmds, env, workingDir, result);
        }
    }

    protected void ensureClientExists() {
    }
}
