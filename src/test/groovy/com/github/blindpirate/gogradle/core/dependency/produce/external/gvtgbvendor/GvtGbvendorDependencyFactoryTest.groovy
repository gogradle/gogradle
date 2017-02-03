package com.github.blindpirate.gogradle.core.dependency.produce.external.gvtgbvendor

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.dependency.produce.external.ExternalDependencyFactoryTest
import com.github.blindpirate.gogradle.util.IOUtils
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks

import static com.github.blindpirate.gogradle.build.Configuration.BUILD

@RunWith(GogradleRunner)
class GvtGbvendorDependencyFactoryTest extends ExternalDependencyFactoryTest {
    @InjectMocks
    GvtGbvendorDependencyFactory factory

    String manifest = '''
            {
                "generator": "github.com/FiloSottile/gvt",
                "dependencies": [
                    {
                        "importpath": "github.com/wadey/gocovmerge",
                        "repository": "https://github.com/wadey/gocovmerge",
                        "vcs": "git",
                        "revision": "b5bfa59ec0adc420475f97f89b58045c721d761c",
                        "branch": "master",
                        "notests": true
                    },
                    {
                        "importpath": "golang.org/x/tools/cmd/goimports",
                        "repository": "https://go.googlesource.com/tools",
                        "vcs": "git",
                        "revision": "8b84dae17391c154ca50b0162662aa1fc9ff84c2",
                        "branch": "master",
                        "path": "/cmd/goimports",
                        "notests": true
                    }
            ]
            }
            '''

    @Test
    void 'reading vendor/manifest should succeed'() {
        // given
        IOUtils.write(resource, 'vendor/manifest', manifest)
        // when
        factory.produce(resource, BUILD)
        // then
        verifyMapParsed([name: 'github.com/wadey/gocovmerge', vcs: 'git', version: 'b5bfa59ec0adc420475f97f89b58045c721d761c'])
        verifyMapParsed([name: 'golang.org/x/tools', vcs: 'git', version: '8b84dae17391c154ca50b0162662aa1fc9ff84c2'])
    }

    @Test
    void 'directory without vendor/manifest should be rejected'() {
        assert !factory.produce(resource, BUILD).isPresent()
    }

    String manifestMissingImportpath = '''
           {
                "generator": "github.com/FiloSottile/gvt",
                "dependencies": [
                    {
                        "repository": "https://github.com/wadey/gocovmerge",
                        "vcs": "git",
                        "revision": "b5bfa59ec0adc420475f97f89b58045c721d761c",
                        "branch": "master",
                        "notests": true
                    } 
            ]
            }
'''

    @Test(expected = RuntimeException)
    void 'missing importpath will result in an exception'() {
        // given
        IOUtils.write(resource, 'vendor/manifest', manifestMissingImportpath)
        // then
        factory.produce(resource, BUILD)
    }

    String manifestWithExtraProperties = '''
            {
                "generator": "github.com/FiloSottile/gvt",
                "extra":1,
                "dependencies": [
                    {
                        "importpath": "github.com/wadey/gocovmerge",
                        "repository": "https://github.com/wadey/gocovmerge",
                        "vcs": "git",
                        "revision": "b5bfa59ec0adc420475f97f89b58045c721d761c",
                        "branch": "master",
                        "notests": true,
                        "extra":{}
                    },
                    {
                        "importpath": "golang.org/x/tools/cmd/goimports",
                        "repository": "https://go.googlesource.com/tools",
                        "vcs": "git",
                        "revision": "8b84dae17391c154ca50b0162662aa1fc9ff84c2",
                        "branch": "master",
                        "path": "/cmd/goimports",
                        "notests": true
                    }
                ]
            }
            '''

    @Test
    void 'extra properties should be ignored'() {
        // given
        IOUtils.write(resource, 'vendor/manifest', manifestWithExtraProperties)
        // when
        factory.produce(resource, BUILD)
        // then
        verifyMapParsed([name: 'github.com/wadey/gocovmerge', vcs: 'git', version: 'b5bfa59ec0adc420475f97f89b58045c721d761c'])
        verifyMapParsed([name: 'golang.org/x/tools', vcs: 'git', version: '8b84dae17391c154ca50b0162662aa1fc9ff84c2'])

    }

}
