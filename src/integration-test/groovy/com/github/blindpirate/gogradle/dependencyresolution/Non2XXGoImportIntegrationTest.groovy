package com.github.blindpirate.gogradle.dependencyresolution

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.support.IntegrationTestSupport
import com.github.blindpirate.gogradle.support.WithIsolatedUserhome
import com.github.blindpirate.gogradle.support.WithMockGo
import com.github.blindpirate.gogradle.support.WithResource
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(GogradleRunner)
@WithResource
@WithMockGo
@WithIsolatedUserhome
class Non2XXGoImportIntegrationTest extends IntegrationTestSupport {
    @Test
    void 'can resolve dependencies whose meta data is in 404 response'() {
        writeBuildAndSettingsDotGradle("""
        ${buildDotGradleBase}

        golang {
            packagePath="github.com/test/package"
        }

        dependencies {
            golang {
                build name: 'gonum.org/v1/gonum/stat/distuv', transitive: false
            }
        }
        """)

        try {
            newBuild('dependencies')
            assert stdout.toString().contains('\\-- gonum.org/v1/gonum')
        } catch (Throwable e) {
            println(stdout)
            println(stderr)
            throw e
        }
    }
}
