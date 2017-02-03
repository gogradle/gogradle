package com.github.blindpirate.gogradle.dependencytest

import org.eclipse.jgit.attributes.AttributesNodeProvider
import org.eclipse.jgit.lib.BaseRepositoryBuilder
import org.eclipse.jgit.lib.ObjectDatabase
import org.eclipse.jgit.lib.RefDatabase
import org.eclipse.jgit.lib.ReflogReader
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.lib.StoredConfig

import java.nio.file.Path

class MockRepository extends Repository {

    File root
    String packageName

    protected MockRepository(File repoRoot, BaseRepositoryBuilder options) {
        super(options)
        this.root = repoRoot
        Path repoPath = repoRoot.toPath()
        // github.com/a/b
        this.packageName = repoPath.subpath(repoPath.nameCount - 3, repoPath.nameCount)
    }

    MockRepository(File repoRoot) {
        this(repoRoot, new BaseRepositoryBuilder())
    }

    long getUpdateTime(String path) {
        try {
            // Unix second
            return new File(root, "${path}/updateTime").getText().toInteger()
        } catch (Exception e) {
            return 0
        }
    }

    @Override
    void create(boolean bare) throws IOException {

    }

    @Override
    ObjectDatabase getObjectDatabase() {
        return null
    }

    @Override
    RefDatabase getRefDatabase() {
        return null
    }

    @Override
    StoredConfig getConfig() {
        return null
    }

    @Override
    AttributesNodeProvider createAttributesNodeProvider() {
        return null
    }

    @Override
    void scanForRepoChanges() throws IOException {

    }

    @Override
    void notifyIndexChanged() {
    }

    @Override
    ReflogReader getReflogReader(String refName) throws IOException {
        return null
    }
}
