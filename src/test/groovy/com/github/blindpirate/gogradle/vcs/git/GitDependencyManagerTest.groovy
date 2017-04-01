package com.github.blindpirate.gogradle.vcs.git

import com.github.blindpirate.gogradle.GogradleRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

@RunWith(GogradleRunner)
class GitDependencyManagerTest {
    @Mock
    GitClientAccessor accessor

    GitDependencyManager manager

    @Before
    void setUp() {
        manager = new GitDependencyManager(null, null, accessor)
    }

    @Test
    void 'getting accessor should succeed'() {
        assert manager.accessor == accessor
    }

}
