package com.github.blindpirate.gogradle.issues

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.support.AccessWeb
import com.github.blindpirate.gogradle.support.IntegrationTestSupport
import com.github.blindpirate.gogradle.support.WithMockGo
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.util.IOUtils
import org.junit.Test
import org.junit.runner.RunWith

@WithResource
@RunWith(GogradleRunner)
@WithMockGo
@AccessWeb
class RecognizeGopkgLockIntegrationTest extends IntegrationTestSupport {
    // https://github.com/gogradle/gogradle/issues/221
    // https://github.com/census-instrumentation/opencensus-go/blob/0edc045e110a4ba034ed03dffbbaf13eeae8b25b/Gopkg.lock
    @Test
    void "can recognize Gopkg_lock's source"() {
        writeBuildAndSettingsDotGradle(buildDotGradleBase)
        IOUtils.write(resource, 'Gopkg.lock', '''
[[projects]]
  branch = "master"
  name = "git.apache.org/thrift.git"
  packages = ["lib/go/thrift"]
  revision = "606f1ef31447526b908244933d5b716397a6bad8"
  source = "github.com/apache/thrift"
''')
        newBuild('goInit')
        newBuild('goVendor')

        assert new File(resource, 'vendor/git.apache.org/thrift.git').exists()
    }

    @Override
    File getProjectRoot() {
        return resource
    }
}
