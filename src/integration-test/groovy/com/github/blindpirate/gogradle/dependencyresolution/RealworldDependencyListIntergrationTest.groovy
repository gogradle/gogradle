package com.github.blindpirate.gogradle.dependencyresolution

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.support.IntegrationTestSupport
import com.github.blindpirate.gogradle.support.WithIsolatedUserhome
import com.github.blindpirate.gogradle.support.WithMockGo
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.util.ProcessUtils
import com.github.blindpirate.gogradle.util.StringUtils
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(GogradleRunner)
@WithResource
@WithMockGo
@WithIsolatedUserhome
class RealworldDependencyListIntergrationTest extends IntegrationTestSupport {
    ProcessUtils processUtils = new ProcessUtils()

    @Test
    void 'list dependencies of nakama'(){
        testOne('github.com/heroiclabs/nakama')
    }

    @Test
    void 'list dependencies of apex'() {
        testOne('github.com/apex/apex')
    }

    void testOne(String repo) {
        processUtils.run('git', 'clone', "https://${repo}.git", StringUtils.toUnixString(resource))
        writeBuildAndSettingsDotGradle("""
        ${buildDotGradleBase}

        golang {
            packagePath="${repo}"
        }
        """)

        try {
            newBuild('init')
            newBuild('dependencies')
        } catch (Throwable e) {
            println(stdout)
            println(stderr)
            throw e
        }
    }
}
