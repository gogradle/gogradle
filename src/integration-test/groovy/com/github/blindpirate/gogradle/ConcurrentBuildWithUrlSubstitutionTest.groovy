package com.github.blindpirate.gogradle

import com.github.blindpirate.gogradle.support.*
import com.github.blindpirate.gogradle.util.IOUtils
import com.github.blindpirate.gogradle.util.StringUtils
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future

@RunWith(GogradleRunner)
@WithResource('')
@WithMockGo
@WithIsolatedUserhome
class ConcurrentBuildWithUrlSubstitutionTest {
    File resource
    ExecutorService threadPool = Executors.newFixedThreadPool(10)

    int TOTAL_THREAD_NUM = 10

    GitServer gitServer

    String goBinPath

    File userhome

    @Before
    void setUp() {
        setUpAndStartGitServer()

        (1..TOTAL_THREAD_NUM).each {
            new File(resource, "project${it}").mkdir()
        }
    }

    void setUpAndStartGitServer() {
        IOUtils.mkdir(resource, 'git-repo/repo1')
        IOUtils.mkdir(resource, 'git-repo/repo2')
        GitServer.createRepository(new File(resource, 'git-repo/repo1'), '1.go')
        GitServer.createRepository(new File(resource, 'git-repo/repo2'), '2.go')
        gitServer = GitServer.newServer(new File(resource, 'git-repo'))
        gitServer.start(GitServer.DEFAULT_PORT)
    }

    @After
    void clearUp() {
        gitServer.stop()
    }

    // 5 concurrent build with original repository
    // 5 concurrent build with substituted repository
    @Test
    void 'concurrent test should succeed'() {
        List<Future> futures = []
        (1..TOTAL_THREAD_NUM / 2).each {
            futures.add(buildWithRepo(it.toInteger(), 'http://localhost:8080/repo1'))
        }
        (TOTAL_THREAD_NUM / 2 + 1..TOTAL_THREAD_NUM).each {
            futures.add(buildWithRepo(it.toInteger(), 'http://localhost:8080/repo2'))
        }

        futures.each { println(it.get()) }

        (1..TOTAL_THREAD_NUM / 2).each {
            assert new File(resource, "project${it}/.gogradle/build_gopath/src/github.com/my/project/1.go").exists()
        }
        (TOTAL_THREAD_NUM / 2 + 1..TOTAL_THREAD_NUM).each {
            assert new File(resource, "project${it}/.gogradle/build_gopath/src/github.com/my/project/2.go").exists()
        }
    }

    Future buildWithRepo(int number, String repoUrl) {
        Callable c = new Callable() {
            Map call() {
                SingleBuild build = new SingleBuild(resource: new File(resource, "project${number}"),
                        goBinPath: goBinPath,
                        userhome: userhome,
                        repoUrl: repoUrl)
                build.baseSetUp()
                build.startBuild()
                return [stdout: build.stdout.toString(), stderr: build.stderr.toString()]
            }
        }

        return threadPool.submit(c)
    }

    class SingleBuild extends IntegrationTestSupport {

        String repoUrl

        @Override
        File getProjectRoot() {
            return resource
        }

        void startBuild() {
            String buildDotGradle = """
buildscript {
    dependencies {
        classpath files(new File(rootDir, '../../../libs/gogradle-${GogradleGlobal.GOGRADLE_VERSION}-all.jar'))
    }
}
System.setProperty('gradle.user.home','${StringUtils.toUnixString(userhome)}')

apply plugin: 'com.github.blindpirate.gogradle'

golang {
    packagePath='my/project'
}

dependencies {
    golang {
        build name:'github.com/my/project',url:'${repoUrl}'
    }
}
golang {
    goExecutable = '${StringUtils.toUnixString(goBinPath)}'
}
"""
            writeBuildAndSettingsDotGradle(buildDotGradle)
            try {
                newBuild {
                    it.forTasks('resolveBuildDependencies')
                }
            } catch (Exception e) {
                e.printStackTrace()
            }
        }
    }
}
