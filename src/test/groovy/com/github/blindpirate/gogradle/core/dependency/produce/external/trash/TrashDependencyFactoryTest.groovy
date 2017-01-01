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
    void 'parsing vendor.conf should success'() {
        // given
        IOUtils.write(resource, 'vendor.conf', '''
# package
github.com/rancher/trash

github.com/Sirupsen/logrus                      v0.10.0
github.com/cloudfoundry-incubator/candiedyaml   99c3df8
github.com/stretchr/testify                     v1.1.3
''')
        // when
        factory.produce(resource)
        // then
        verifyMapParsed([name: 'github.com/Sirupsen/logrus', tag: 'v0.10.0'])
        verifyMapParsed([name: 'github.com/cloudfoundry-incubator/candiedyaml', version: '99c3df8'])
        verifyMapParsed([name: 'github.com/stretchr/testify', tag: 'v1.1.3'])

    }
}
