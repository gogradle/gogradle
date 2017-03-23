package com.github.blindpirate.gogradle.dependencytest

import com.github.blindpirate.gogradle.GolangRepositoryHandler
import com.github.blindpirate.gogradle.util.IOUtils
import com.github.blindpirate.gogradle.vcs.git.GitAccessor
import com.github.blindpirate.gogradle.vcs.git.GitDependencyManager
import com.github.blindpirate.gogradle.vcs.git.RevCommitUtils
import org.apache.commons.io.FileUtils
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevCommit

import static org.mockito.Mockito.mock

class MockGitAccessor extends GitAccessor {

    File mockGitRepo
    // [github.com/a/b:[commit1,commit2,commit3]]
    Map packages
    //
    Map tags = ['github.com/firstlevel/c@1.0.0': 'commit3']

    MockGitAccessor(String mockGitRepoPath) {
        super(mock(GolangRepositoryHandler))
        this.mockGitRepo = new File(mockGitRepoPath)
        if (mockGitRepo.list() != null) {
            packages = mockGitRepo.list().collect {
                directoryNameToPackageNameAndCommit(it)
            }.groupBy {
                it.packageName
            }.collectEntries { packageName, packageNameAndCommits ->
                List commits = packageNameAndCommits.collect { it.commit }
                return [packageName, commits.toSorted()]
            }
        }
    }

    static directoryNameToPackageNameAndCommit(String dirName) {
        int lastUnderscore = dirName.lastIndexOf('_')
        return [packageName: directoryNameToPackageName(dirName[0..lastUnderscore - 1]),
                commit     : dirName[lastUnderscore + 1..-1]]
    }

    static directoryNameToPackageName(String dirName) {
        return dirName.replaceAll('_', '/')
    }

    // git@github.com:a/b.git -> github.com/a/b
    static urlToPackageName(String gitUrl) {
        return gitUrl[4..-5].replace(":", "/")
    }

    String getLatestCommit(String packageName) {
        return packages[packageName][-1]
    }

    static directoryToPackageName(File dir) {
        return directoryNameToPackageName(dir.name)
    }

    File packageNameAndCommitToDir(String packageName, String commit) {
        return new File(mockGitRepo, packageName.replaceAll('/', '_') + "_${commit}")
    }

    @Override
    String getRemoteUrl(File directory) {
        return [getRemoteUrl(new MockRepository(directory))]
    }

    Repository getRepository(File directory) {
        return new MockRepository(directory)
    }

    Set<String> getRemoteUrls(Repository repository) {
        return [getRemoteUrl(repository)] as Set
    }

    void clone(String name, String url, File directory) {
        String packageName = urlToPackageName(url)
        copyLatestCommitTo(packageName, directory)
    }

    void copyLatestCommitTo(String packageName, File directory) {
        FileUtils.cleanDirectory(directory)
        List commits = packages[packageName]
        File latestCommitDirectory = packageNameAndCommitToDir(packageName, commits[-1])
        IOUtils.copyDirectory(latestCommitDirectory, directory)
    }

    Optional<RevCommit> headCommitOfBranch(Repository repository, String branch) {
        String packageName = repository.packageName
        // commit1
        String latestCommit = getLatestCommit(packageName)
        // 000000000..01
        return Optional.of(commitXToCommitSha(repository, latestCommit))
    }

    RevCommit commitXToCommitSha(Repository repository, String commitX) {
        // commit2 -> 200000..0
        int x = commitX[-1].toInteger()
        return RevCommitUtils.of(commitX[-1] + '0' * 39, x)
    }

    void checkout(Repository repository, String commitSha) {
        if (commitSha == GitDependencyManager.DEFAULT_BRANCH) {
            return
        }

        FileUtils.cleanDirectory(repository.root)
        // 100000..0 -> commit1
        String realCommitId = 'commit' + commitSha[0]
        File directoryOfThatCommit = packageNameAndCommitToDir(repository.packageName, realCommitId)
        IOUtils.copyDirectory(directoryOfThatCommit, repository.root)
    }

    //github.com/a/b -> https://github.com/a/b.git
    String getRemoteUrl(Repository repository) {
        String tmp = repository.packageName.replaceFirst("/", ":")
        return "git@${tmp}.git"
    }

    Optional<RevCommit> findCommit(Repository repository, String commitX) {
        List commits = packages[repository.packageName]
        return commits.contains(commitX) ? Optional.of(commitXToCommitSha(repository, commitX)) : Optional.empty()
    }

    Repository hardResetAndPull(String name, Repository repository) {
        copyLatestCommitTo(repository.packageName, repository.root)
        return repository
    }

    Optional<RevCommit> findCommitByTag(Repository repository, String tag) {
        String packageName = repository.packageName
        String key = "${packageName}@${tag}"

        if (tags.containsKey(key)) {
            String commitX = tags[key]
            return Optional.of(commitXToCommitSha(repository, commitX))
        } else {
            return Optional.empty()
        }
    }

    long lastCommitTimeOfPath(Repository repository, String path) {
        // MockRepository
        return 1000 * repository.getUpdateTime(path)
    }

}
