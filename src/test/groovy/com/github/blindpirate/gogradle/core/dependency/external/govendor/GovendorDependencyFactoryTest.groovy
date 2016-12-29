package com.github.blindpirate.gogradle.core.dependency.external.govendor

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.WithResource
import com.github.blindpirate.gogradle.core.GolangPackageModule
import com.github.blindpirate.gogradle.util.IOUtils
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito

import static org.mockito.Mockito.*

@RunWith(GogradleRunner)
class GovendorDependencyFactoryTest {
    @Mock
    GolangPackageModule module

    File resource

    GovendorDependencyFactory factory = new GovendorDependencyFactory()

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
                    },
                    {
                        "checksumSHA1": "3VJcSYFds0zeIO5opOs0AoKm3Mw=",
                        "path": "github.com/google/shlex",
                        "revision": "6f45313302b9c56850fc17f99e40caebce98c716",
                        "revisionTime": "2015-01-27T13:39:51Z"
                    },
                    {
                        "checksumSHA1": "GcaTbmmzSGqTb2X6qnNtmDyew1Q=",
                        "path": "github.com/pkg/errors",
                        "revision": "a2d6902c6d2a2f194eb3fb474981ab7867c81505",
                        "revisionTime": "2016-06-27T22:23:52Z"
                    },
                    {
                        "checksumSHA1": "uwKP1AVzd+lrTMlXVFjZXXHzB7U=",
                        "path": "golang.org/x/tools/go/vcs",
                        "revision": "1727758746e7a08feaaceb9366d1468498ac2ac2",
                        "revisionTime": "2016-06-24T22:27:06Z"
                    }
            ],
                "rootPath": "github.com/kardianos/govendor"
        }
    '''

    @Test
    @Ignore
    @WithResource('')
    void 'reading vendor/vendor.json should success'() {
        // given
        IOUtils.write(resource, 'vendor/vendor.json', vendorDotJson)
        when(module.getRootDir()).thenReturn(resource.toPath())
        factory.produce(module)
    }
}
