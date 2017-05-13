package com.github.blindpirate.gogradle.vcs

import org.junit.Test

class GitMercurialCommitTest {
    @Test
    void 'checking sem version should succeed'() {
        assert GitMercurialCommit.of('id', '1.2.3', 0L).satisfies('v1.2.3')
        assert GitMercurialCommit.of('id', 'v1.2.3', 0L).satisfies('1.2.3')
        assert GitMercurialCommit.of('id', 'v1.2.3', 0L).satisfies('v1.x')
        assert GitMercurialCommit.of('id', 'v1.2.3', 0L).satisfies('1.x')
        assert GitMercurialCommit.of('id', 'v1.2.3', 0L).satisfies('v1.X')
        assert GitMercurialCommit.of('id', 'v1.2.3', 0L).satisfies('1.X')
        assert GitMercurialCommit.of('id', 'v1.2.3', 0L).satisfies('v1.*')
        assert GitMercurialCommit.of('id', 'v1.2.3', 0L).satisfies('1.*')
        assert GitMercurialCommit.of('id', 'v1.2.3', 0L).satisfies('v1')
        assert GitMercurialCommit.of('id', 'v1.2.3', 0L).satisfies('1')

        assert GitMercurialCommit.of('id', 'v1.2.3', 0L).satisfies('~1.2')
        assert GitMercurialCommit.of('id', 'v1.2.3', 0L).satisfies('1.0-1.3')
        assert GitMercurialCommit.of('id', 'v1.2.3', 0L).satisfies('!(2.x)')
        assert GitMercurialCommit.of('id', 'v1.2.3', 0L).satisfies('~2.0 | (1.4.* & !=1.4.5) | ~1.2')
    }
}
