package com.github.blindpirate.gogradle.core.dependency.produce.external.glide

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.dependency.produce.external.ExternalDependencyFactoryTest
import com.github.blindpirate.gogradle.util.IOUtils
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks

@RunWith(GogradleRunner)
class GlideDependencyFactoryTest extends ExternalDependencyFactoryTest {

    @InjectMocks
    GlideDependencyFactory factory

    @Test
    void 'test dependencies should be empty'() {
        prepareGlideDotLock(glideDotLock)
        assert factory.produce(resource, 'test').get().isEmpty()
    }

    @Test
    void 'package without glide.lock should be rejected'() {
        assert !factory.produce(resource, 'build').isPresent()
    }

    String glideDotLock = '''
hash: 67c5571c33bfcb663d32d2b40b9ce1f2a05a3fa2e9f442077277c2782195729c
updated: 2016-08-11T14:22:17.773372627-04:00
imports:
- name: github.com/codegangsta/cli
  version: 1efa31f08b9333f1bd4882d61f9d668a70cd902e
- name: github.com/Masterminds/semver
  version: 8d0431362b544d1a3536cca26684828866a7de09
- name: github.com/Masterminds/vcs
  version: fbe9fb6ad5b5f35b3e82a7c21123cfc526cbf895
- name: gopkg.in/yaml.v2
  version: e4d366fc3c7938e2958e662b4258c7a89e1f0e3e
testImports: []
'''

    @Test
    void 'parsing glide.lock should succeed'() {
        // given
        prepareGlideDotLock(glideDotLock)

        // when
        factory.produce(resource, 'build')
        // then
        verifyMapParsed([name: 'github.com/codegangsta/cli', version: '1efa31f08b9333f1bd4882d61f9d668a70cd902e'])
        verifyMapParsed([name: 'github.com/Masterminds/semver', version: '8d0431362b544d1a3536cca26684828866a7de09'])
        verifyMapParsed([name: 'github.com/Masterminds/vcs', version: 'fbe9fb6ad5b5f35b3e82a7c21123cfc526cbf895'])
        verifyMapParsed([name: 'gopkg.in/yaml.v2', version: 'e4d366fc3c7938e2958e662b4258c7a89e1f0e3e'])
    }

    String glideDotLockMissingName = '''
hash: 67c5571c33bfcb663d32d2b40b9ce1f2a05a3fa2e9f442077277c2782195729c
updated: 2016-08-11T14:22:17.773372627-04:00
imports:
- version: 1efa31f08b9333f1bd4882d61f9d668a70cd902e
testImports: []
'''

    @Test(expected = IllegalStateException)
    void 'missing name should cause an exception'() {
        // given
        prepareGlideDotLock(glideDotLockMissingName)
        // then
        factory.produce(resource, 'build')
    }

    String glideDotLockMissingVersion = '''
hash: 67c5571c33bfcb663d32d2b40b9ce1f2a05a3fa2e9f442077277c2782195729c
updated: 2016-08-11T14:22:17.773372627-04:00
imports:
- name: github.com/codegangsta/cli
testImports: []
'''

    @Test
    void 'missing version should not cause an exception'() {
        // given
        prepareGlideDotLock(glideDotLockMissingVersion)
        // when
        factory.produce(resource, 'build')
        // then
        verifyMapParsed([name: 'github.com/codegangsta/cli'])
    }

    String glideDotLockWithExtraAndMissingProperties = '''
wtf: This is an extra property
imports:
- name: github.com/codegangsta/cli
  version: 1efa31f08b9333f1bd4882d61f9d668a70cd902e
  wtf: xxx
'''

    @Test
    void 'extra properties in glide.lock should be success'() {
        // given
        prepareGlideDotLock(glideDotLockWithExtraAndMissingProperties)
        // when
        factory.produce(resource, 'build')
        // then

        verifyMapParsed([name: 'github.com/codegangsta/cli', version: '1efa31f08b9333f1bd4882d61f9d668a70cd902e'])
    }


    void prepareGlideDotLock(String glideDotLock) {
        IOUtils.write(resource, 'glide.lock', glideDotLock)
    }
}
