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

package com.github.blindpirate.gogradle.vcs.git

import com.github.blindpirate.gogradle.util.ReflectionUtils
import com.github.blindpirate.gogradle.vcs.VcsType
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
        repository.root('github.com/a/b')
        assert repository.match('github.com/a/b')
        assert !repository.match('github.com/c/d')
    }

    @Test
    void 'getting url should succeed'() {
        repository.url('123')
        assert repository.getUrl(null) == '123'

        repository.url {
            return '456'
        }

        assert repository.getUrl(null) == '456'

        repository.url { name ->
            return "${name}789"
        }
        assert repository.getUrl('name') instanceof String
        assert repository.getUrl('name') == 'name789'

        repository.url(1)
        assert repository.getUrl('name') == null
    }

    @Test
    void 'getting dir should succeed'() {
        repository.dir('123')
        assert repository.getDir(null) == '123'

        repository.dir {
            return '456'
        }

        assert repository.getDir(null) == '456'

        repository.dir { name ->
            return name + '789'
        }
        assert repository.getDir('name') == 'name789'

        repository.dir(1)
        assert repository.getDir('name') == null

        repository.emptyDir()
        assert repository.getDir(null) == GolangRepository.EMPTY_DIR
    }

    @Test
    void 'getting vcs should succeed'() {
        assert repository.getVcsType() == VcsType.GIT

        repository.vcs 'hg'
        assert repository.getVcsType() == VcsType.MERCURIAL
    }

    @Test
    void 'a repository with pattern name should match'() {
        repository.root(~/github\.com.*/)
        assert repository.match('github.com/a/b')
        assert repository.match('github.com/c/d')
        assert !repository.match('www.github.com/a/b')
    }

    @Test
    void 'a repository with closure name should match'() {
        repository.root {
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
                GolangRepository, ['getUrl', 'match', 'getDir', 'getVcsType'])
    }
}
