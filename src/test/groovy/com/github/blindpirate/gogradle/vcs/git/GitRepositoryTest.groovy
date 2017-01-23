package com.github.blindpirate.gogradle.vcs.git

import org.junit.Test

class GitRepositoryTest {
    GitRepository repository = new GitRepository()

    @Test
    void 'a repository should match all repos if declared as `all`'() {
        repository.all()
        assert repository.match('', '')
    }

    @Test
    void 'a repository with string name should match'() {
        repository.name('github.com/a/b')
        assert repository.match('github.com/a/b', 'url')
        assert !repository.match('github.com/c/d', 'url')
    }

    @Test
    void 'a repository with string url should match'() {
        repository.url('https://github.com/a/b.git')
        assert repository.match('github.com/a/b', 'https://github.com/a/b.git')
        assert !repository.match('github.com/a/b', 'http://github.com/a/b.git')
    }

    @Test
    void 'a repository with pattern name should match'() {
        repository.name(~/github\.com.*/)
        assert repository.match('github.com/a/b', 'url')
        assert repository.match('github.com/c/d', 'url')
        assert !repository.match('www.github.com/a/b', 'url')
    }

    @Test
    void 'a repository with closure name should match'() {
        repository.name {
            it.endsWith('b')
        }
        assert repository.match('b', 'url')
        assert repository.match('github.com/a/b', 'url')
        assert !repository.match('github.com/c/d', 'url')
    }

    @Test
    void 'setting username and password via credentials should succeed'() {
        repository.credentials {
            username 'username'
            password 'password'
        }

        assert repository.username == 'username'
        assert repository.password == 'password'
    }

    @Test
    void 'setting privateKeyFile via credentials should success'() {
        repository.credentials {
            privateKeyFile 'path/to/key'
        }
        assert repository.privateKeyFilePath == 'path/to/key'
    }

    @Test(expected = IllegalStateException)
    void 'exception should be thrown if url and name both blank'() {
        repository.match('', '')
    }
}
