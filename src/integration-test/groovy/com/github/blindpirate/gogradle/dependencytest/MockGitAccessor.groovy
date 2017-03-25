package com.github.blindpirate.gogradle.dependencytest

import com.github.blindpirate.gogradle.util.IOUtils
import com.github.blindpirate.gogradle.util.StringUtils
import com.github.blindpirate.gogradle.vcs.GitMercurialCommit
import com.github.blindpirate.gogradle.vcs.git.GitClientAccessor
import com.github.blindpirate.gogradle.vcs.git.GitDependencyManager
import org.apache.commons.io.FileUtils

import java.nio.file.Path

class MockGitAccessor extends GitClientAccessor {

    File mockGitRepo
    // [github.com/a/b:[commit1,commit2,commit3]]
    Map packages
    //
    Map tags = ['github.com/firstlevel/c@1.0.0': 'commit3']

    MockGitAccessor(String mockGitRepoPath) {
        super(null)
        this.mockGitRepo = new File(mockGitRepoPath)
        if (mockGitRepo.list() != null) {
            packages = mockGitRepo.list().collect {
                repoDirNameToPkgNameAndCommit(it)
            }.groupBy {
                it.packageName
            }.collectEntries { packageName, packageNameAndCommits ->
                List commits = packageNameAndCommits.collect { it.commit }
                return [packageName, commits.toSorted()]
            }
        }
    }

    @Override
    protected void ensureClientExists() {

    }

    static repoDirNameToPkgNameAndCommit(String dirName) {
        int lastUnderscore = dirName.lastIndexOf('_')
        return [packageName: repoDirNameToPackageName(dirName[0..lastUnderscore - 1]),
                commit     : dirName[lastUnderscore + 1..-1]]
    }

    static globalCacheDirToRootPkgName(File repoRoot) {
        Path repoPath = repoRoot.toPath()
        // github.com/a/b
        return StringUtils.toUnixString(repoPath.subpath(repoPath.nameCount - 3, repoPath.nameCount))
    }

    static repoDirNameToPackageName(String dirName) {
        return dirName.replaceAll('_', '/')
    }

    // git@github.com:a/b.git -> github.com/a/b
    static urlToPackageName(String gitUrl) {
        return gitUrl[4..-5].replace(":", "/")
    }

    String getLatestCommit(String packageName) {
        println("package:${packageName}")
        return packages[packageName][-1]
    }

    File packageNameAndCommitToDir(String packageName, String commit) {
        return new File(mockGitRepo, packageName.replaceAll('/', '_') + "_${commit}")
    }

    @Override
    void clone(String url, File directory) {
        String packageName = urlToPackageName(url)
        copyLatestCommitTo(packageName, directory)
    }

    void copyLatestCommitTo(String packageName, File directory) {
        FileUtils.cleanDirectory(directory)
        List commits = packages[packageName]
        File latestCommitDirectory = packageNameAndCommitToDir(packageName, commits[-1])
        IOUtils.copyDirectory(latestCommitDirectory, directory)
    }

    @Override
    GitMercurialCommit headCommitOfBranch(File repository, String branch) {
        String packageName = globalCacheDirToRootPkgName(repository)
        // commit1
        String latestCommit = getLatestCommit(packageName)
        // 000000000..01
        return commitXToCommitSha(repository, latestCommit)
    }

    GitMercurialCommit commitXToCommitSha(File repository, String commitX) {
        // commit2 -> 200000..0
        int x = commitX[-1].toInteger()
        return GitMercurialCommit.of(commitX[-1] + '0' * 39, x * 1000L)
    }

    void checkout(File repository, String commitSha) {
        if (commitSha == GitDependencyManager.DEFAULT_BRANCH) {
            return
        }

        FileUtils.cleanDirectory(repository)
        // 100000..0 -> commit1
        String realCommitId = 'commit' + commitSha[0]
        File directoryOfThatCommit = packageNameAndCommitToDir(globalCacheDirToRootPkgName(repository), realCommitId)
        IOUtils.copyDirectory(directoryOfThatCommit, repository)
    }

    //github.com/a/b -> https://github.com/a/b.git
    String getRemoteUrl(File repository) {
        String tmp = globalCacheDirToRootPkgName(repository).replaceFirst("/", ":")
        return "git@${tmp}.git"
    }

    Optional<GitMercurialCommit> findCommit(File repository, String commitX) {
        List commits = packages[globalCacheDirToRootPkgName(repository)]
        return commits.contains(commitX) ? Optional.of(commitXToCommitSha(repository, commitX)) : Optional.empty()
    }

    @Override
    void pull(File repository) {
        copyLatestCommitTo(globalCacheDirToRootPkgName(repository), repository)
    }

    @Override
    Optional<GitMercurialCommit> findCommitByTag(File repository, String tag) {
        String packageName = globalCacheDirToRootPkgName(repository)
        String key = "${packageName}@${tag}"

        if (tags.containsKey(key)) {
            String commitX = tags[key]
            return Optional.of(commitXToCommitSha(repository, commitX))
        } else {
            return Optional.empty()
        }
    }

    @Override
    long lastCommitTimeOfPath(File repository, String path) {
        try {
            // Unix second
            int seconds = new File(repository, "${path}/updateTime").getText().toInteger()
            return 1000 * seconds
        } catch (Exception e) {
            return 0
        }
    }

}
