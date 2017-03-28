package com.github.blindpirate.gogradle.vcs.git

import com.github.blindpirate.gogradle.util.ReflectionUtils
import org.junit.Test

class GolangRepositoryTest {
    GolangRepository repository = new GolangRepository()

    @Test
    void 'a repository should match all repos if declared as `all`'() {
        repository.all()
        assert repository.match('')
    }

    @Test
    void 'a repository with string name should match'() {
        repository.name('github.com/a/b')
        assert repository.match('github.com/a/b')
        assert !repository.match('github.com/c/d')
    }

    @Test
    void 'substitute url should succeed'() {
        repository.urlSubstitution('123')
        assert repository.substitute(null, null) == '123'

        repository.urlSubstitution {
            return '456'
        }

        assert repository.substitute(null, null) == '456'

        repository.urlSubstitution { name ->
            return name + '789'
        }
        assert repository.substitute('name', null) == 'name789'

        repository.urlSubstitution { name, url ->
            return name + url
        }
        assert repository.substitute('name', 'url') == 'nameurl'

        repository.urlSubstitution { a, b, c ->
            return 'ShouldNotTakeEffect'
        }
        assert repository.substitute('name', 'url') == 'url'
    }

    @Test
    void 'a repository with pattern name should match'() {
        repository.name(~/github\.com.*/)
        assert repository.match('github.com/a/b')
        assert repository.match('github.com/c/d')
        assert !repository.match('www.github.com/a/b')
    }

    @Test
    void 'a repository with closure name should match'() {
        repository.name {
            it.endsWith('b')
        }
        assert repository.match('b')
        assert repository.match('github.com/a/b')
        assert !repository.match('github.com/c/d')
    }

    @Test(expected = IllegalStateException)
    void 'exception should be thrown if url and name both blank'() {
        repository.match('')
    }

    @Test
    void 'global GolangRepository singleton should be read-only'() {
        ReflectionUtils.testUnsupportedMethods(GolangRepository.EMPTY_INSTANCE,
                GolangRepository, ['substitute', 'match'])
    }
}
