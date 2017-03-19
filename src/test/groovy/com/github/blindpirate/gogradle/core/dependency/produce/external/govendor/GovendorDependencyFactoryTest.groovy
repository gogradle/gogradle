package com.github.blindpirate.gogradle.core.dependency.produce.external.govendor

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.dependency.produce.external.ExternalDependencyFactoryTest
import com.github.blindpirate.gogradle.util.IOUtils
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks


@RunWith(GogradleRunner)
class GovendorDependencyFactoryTest extends ExternalDependencyFactoryTest {
    @InjectMocks
    GovendorDependencyFactory factory
    String vendorDotJson = '''
            {
                "comment": "",
                "ignore": "test",
                "package": [
                    {
                        "checksumSHA1": "rcwA7Jmo3eZ4bEQb8mTI78haZfc=",
                        "path": "github.com/Bowery/prompt",
                        "revision": "d43c2707a6c5a152a344c64bb4fed657e2908a81",
                        "revisionTime": "2016-08-08T16:52:56Z"
                    },
                    {
                        "checksumSHA1": "6VGFARaK8zd23IAiDf7a+gglC8k=",
                        "path": "github.com/dchest/safefile",
                        "revision": "855e8d98f1852d48dde521e0522408d1fe7e836a",
                        "revisionTime": "2015-10-22T12:31:44+02:00"
                    } 
            ],
                "rootPath": "github.com/kardianos/govendor"
        }
    '''

    @Test
    void 'package without vendor/vendor.json should be rejected'() {
        assert !factory.produce(resource, 'build').isPresent()
    }

    @Test
    void 'reading vendor/vendor.json should succeed'() {
        // given
        prepareVendorDotJson(vendorDotJson)
        // when
        factory.produce(resource, 'build')
        // then
        verifyMapParsed([name: 'github.com/Bowery/prompt', version: 'd43c2707a6c5a152a344c64bb4fed657e2908a81'])
        verifyMapParsed([name: 'github.com/dchest/safefile', version: '855e8d98f1852d48dde521e0522408d1fe7e836a'])
    }

    @Test(expected = RuntimeException)
    void 'corrupted vendor.json should cause an exception'() {
        // given
        prepareVendorDotJson('This is corrupted')
        // then
        factory.produce(resource, 'build')
    }

    @Test(expected = IllegalStateException)
    void 'missing path should cause an exception'() {
        // given
        prepareVendorDotJson('{"package":[{}]}')
        // then
        factory.produce(resource, 'build')
    }

    @Test
    void 'missing revision should not cause an exception'() {
        // given
        prepareVendorDotJson('{"package":[{"path":"a"}]}')
        // when
        factory.produce(resource, 'build')
        // then
        verifyMapParsed([name: 'a'])
    }
    String vendorDotJsonWithExtraProperties = '''
    {
                "comment": "",
                "ignore": "test",
                "a":[],
                "package": [
                    {
                        "checksumSHA1": "rcwA7Jmo3eZ4bEQb8mTI78haZfc=",
                        "path": "github.com/Bowery/prompt",
                        "revision": "d43c2707a6c5a152a344c64bb4fed657e2908a81",
                        "revisionTime": "2016-08-08T16:52:56Z",
                        "a":"1"
                    } 
            ],
                "rootPath": "github.com/kardianos/govendor"
        }
'''

    @Test
    void 'extra properties should be ignored'() {
        // given
        prepareVendorDotJson(vendorDotJsonWithExtraProperties)
        // then
        factory.produce(resource, 'build')
    }

    void prepareVendorDotJson(String s) {
        IOUtils.write(resource, "vendor/vendor.json", s)
    }
}
