package com.github.blindpirate.gogradle.core.dependency.produce.external.trash

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.dependency.produce.external.ExternalDependencyFactoryTest
import com.github.blindpirate.gogradle.util.IOUtils
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks


@RunWith(GogradleRunner)
class TrashDependencyFactoryTest extends ExternalDependencyFactoryTest {

    @InjectMocks
    TrashDependencyFactory factory

    @Test
    void 'parsing vendor.conf should succeed'() {
        // given
        IOUtils.write(resource, 'vendor.conf', '''
# package
github.com/rancher/trash

github.com/Sirupsen/logrus                      v0.10.0
github.com/cloudfoundry-incubator/candiedyaml   99c3df8
github.com/stretchr/testify                     v1.1.3
github.com/go-check/check 4ed411733c5785b40214c70bce814c3a3a689609 https://github.com/cpuguy83/check.git
''')
        // when
        factory.produce(resource, 'build')
        // then
        verifyMapParsed([name: 'github.com/Sirupsen/logrus', tag: 'v0.10.0'])
        verifyMapParsed([name: 'github.com/cloudfoundry-incubator/candiedyaml', version: '99c3df8'])
        verifyMapParsed([name: 'github.com/stretchr/testify', tag: 'v1.1.3'])
        verifyMapParsed([name   : 'github.com/go-check/check',
                         version: '4ed411733c5785b40214c70bce814c3a3a689609',
                         url    : 'https://github.com/cpuguy83/check.git'])

    }
}
