package com.github.blindpirate.gogradle.packageresolution

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.cache.GlobalCacheMetadata
import com.github.blindpirate.gogradle.support.*
import com.github.blindpirate.gogradle.util.DataExchange
import com.github.blindpirate.gogradle.util.IOUtils
import com.github.tomakehurst.wiremock.junit.WireMockRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

import static com.github.tomakehurst.wiremock.client.WireMock.*
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig

@RunWith(GogradleRunner)
@WithResource
@WithIsolatedUserhome
@WithMockGo
@OnlyOnPosix
@WithGitRepo(repoName = 'a', fileName = 'commit1.go')
class PackageReslutionIntegrationTest extends IntegrationTestSupport {
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().dynamicPort())

    int port

    @Before
    void setup() {
        port = wireMockRule.port()
        writeBuildAndSettingsDotGradle("""
${buildDotGradleBase}

dependencies {
    golang {
        build name: 'localhost:${port}/a'
    }
}
""")
    }


    @Test
    void 'unknown path could be resolved via http'() {
        // given
        setStub()

        // when
        newBuild('dependencies')

        // then
        verify(1, getRequestedFor(urlEqualTo('/a?go-get=1')))
        def metadata = DataExchange.parseYaml(new File(userhome, "go/repo/localhost:${port}/a/gogradle-metadata").text, GlobalCacheMetadata)
        assert metadata.pkg == "localhost:${port}/a"
        assert metadata.repositories.size() == 1
        assert System.currentTimeMillis() - metadata.repositories[0].lastUpdatedTime < 5000
        assert new File(userhome, "go/repo/localhost:${port}/a/${metadata.repositories[0].dir}/commit1.go").exists()
        assert metadata.repositories[0].original
        assert metadata.repositories[0].vcs == 'git'
        assert metadata.repositories[0].urls == ["http://localhost:${GitServer.DEFAULT_PORT}/a" as String]
    }

    def setStub() {
        stubFor(get(urlEqualTo('/a?go-get=1'))
                .willReturn(aResponse().withHeader('Content-Type', 'text/html').withBody("""
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
<meta name="go-import" content="localhost:${port}/a git http://localhost:${GitServer.DEFAULT_PORT}/a">
</head>
<body>
Nothing to see here; 
</body>
</html>
""")))
    }

    @Test
    void 'unknown path could be resolved via local cache metadata'() {
        // given
        writeMetadata(true)

        // when
        newBuild('vendor')

        // then
        verify(0, getRequestedFor(anyUrl()))
        assert new File(userhome, "go/repo/localhost:${port}/a/1a2b3c4d/commit1.go").exists()
        assert new File(projectRoot, "vendor/localhost:${port}/a/commit1.go").exists()
    }

    @Test
    void 'unknown path could not be resolved via local cache metadata with original=false'() {
        // given
        setStub()
        writeMetadata("http://anotherUrl", false)

        // when
        newBuild('dependencies')

        // then
        verify(1, getRequestedFor(urlEqualTo('/a?go-get=1')))
        def metadata = DataExchange.parseYaml(new File(userhome, "go/repo/localhost:${port}/a/gogradle-metadata").text, GlobalCacheMetadata)
        assert metadata.pkg == "localhost:${port}/a"
        assert metadata.repositories.size() == 2
        assert System.currentTimeMillis() - metadata.repositories[1].lastUpdatedTime < 5000
        assert new File(userhome, "go/repo/localhost:${port}/a/${metadata.repositories[1].dir}/commit1.go").exists()
        assert metadata.repositories[1].original
        assert metadata.repositories[1].vcs == 'git'
        assert metadata.repositories[1].urls == ["http://localhost:${GitServer.DEFAULT_PORT}/a" as String]
    }

    def writeMetadata(boolean original) {
        writeMetadata("http://localhost:${GitServer.DEFAULT_PORT}/a", original)
    }

    def writeMetadata(String url, boolean original) {
        IOUtils.write(userhome, "go/repo/localhost:${port}/a/gogradle-metadata", """
---
apiVersion: "0.8.0"
package: "localhost:${port}/a"
repositories:
- vcs: "git"
  urls:
  - "${url}"
  lastUpdatedTime: ${System.currentTimeMillis()}
  dir: 1a2b3c4d
  original: ${original}
""")
    }

    @Override
    File getProjectRoot() {
        return resource
    }
}
