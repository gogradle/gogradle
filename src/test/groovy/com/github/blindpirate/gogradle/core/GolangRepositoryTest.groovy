package com.github.blindpirate.gogradle.core

import org.junit.Test

import static com.github.blindpirate.gogradle.core.GolangRepository.newOriginalRepository
import static com.github.blindpirate.gogradle.core.GolangRepository.newSubstitutedRepository
import static com.github.blindpirate.gogradle.vcs.VcsType.GIT
import static com.github.blindpirate.gogradle.vcs.VcsType.MERCURIAL

class GolangRepositoryTest {

    @Test
    void 'repository containing same urls should be considered as equal'() {
        assert newOriginalRepository(GIT, 'url1') == newOriginalRepository(GIT, ['url1'])
        assert newOriginalRepository(GIT, 'url1') == newOriginalRepository(GIT, ['url1', 'url2'])
        assert newOriginalRepository(GIT, 'url1') != newOriginalRepository(GIT, ['url2'])
        assert newOriginalRepository(GIT, 'url1') == newSubstitutedRepository(GIT, ['url1', 'url2'])
        assert newOriginalRepository(GIT, 'url1') != newOriginalRepository(MERCURIAL, ['url1'])
    }

    @Test
    void 'only vcs is considered in hashCode'() {
        assert newOriginalRepository(GIT, 'url1').hashCode() == newOriginalRepository(GIT, 'url2').hashCode()
        assert newOriginalRepository(GIT, 'url1').hashCode() == newSubstitutedRepository(GIT, ['url2']).hashCode()
        assert newOriginalRepository(GIT, 'url1').hashCode() != newOriginalRepository(MERCURIAL, 'url2').hashCode()
    }
}