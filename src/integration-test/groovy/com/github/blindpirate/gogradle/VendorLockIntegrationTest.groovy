package com.github.blindpirate.gogradle

import com.github.blindpirate.gogradle.core.dependency.lock.GogradleLockModel
import com.github.blindpirate.gogradle.support.GitServer
import com.github.blindpirate.gogradle.support.IntegrationTestSupport
import com.github.blindpirate.gogradle.support.WithGitRepo
import com.github.blindpirate.gogradle.support.WithIsolatedUserhome
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.util.DataExchange
import com.github.blindpirate.gogradle.util.IOUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(GogradleRunner)
@WithResource('')
@WithIsolatedUserhome
@WithGitRepo(repoName = 'dependency1', fileName = 'commit1.go')
class VendorLockIntegrationTest extends IntegrationTestSupport {
    File repositories

    @Before
    void setUp() {
        writeBuildAndSettingsDotGradle("""
${buildDotGradleBase}
golang {
    packagePath='github.com/my/package'
}

repositories {
    golang {
        root 'github.com/my/dependency1'
        url 'http://localhost:8080/dependency1'
    }
}

dependencies {
    golang {
        build 'github.com/my/dependency1'
    }
}
""")
    }

    @Test
    void 'locking dependency in vendor should succeed'() {
        IOUtils.write(resource, 'vendor/github.com/my/dependency1/vendor.go', '')

        build('goLock', 'installBuildDependencies')
        assertLockIsVendor()
        assert new File(resource, '.gogradle/build_gopath/src/github.com/my/dependency1/vendor.go').exists()

        build('goLock', 'installBuildDependencies')
        assert new File(resource, '.gogradle/build_gopath/src/github.com/my/dependency1/vendor.go').exists()
        assertLockIsVendor()

        deleteDotGogradle()
        build('goLock', 'installBuildDependencies')
        assert new File(resource, '.gogradle/build_gopath/src/github.com/my/dependency1/vendor.go').exists()
        assertLockIsVendor()

        IOUtils.append(new File(resource, 'build.gradle'), 'golang {buildMode="DEVELOP"}')
        build('goLock', 'installBuildDependencies')
        assert new File(resource, '.gogradle/build_gopath/src/github.com/my/dependency1/commit1.go').exists()
        assertLockIsVcs()
    }

    @Test
    void 'persistent cache should be used'() {
        build('goLock', 'installBuildDependencies')
        assertLockIsVcs()

        build('installBuildDependencies')

        deleteDependencyTreeCache()
        build('installBuildDependencies')
        assert new File(resource, '.gogradle/build_gopath/src/github.com/my/dependency1/commit1.go').exists()
        assert stdout.toString().contains('Resolving cached')
    }

    void assertLockIsVcs() {
        GogradleLockModel model = DataExchange.parseYaml(new File(resource, 'gogradle.lock'), GogradleLockModel)
        Map dependency = model.getDependencies('build')[0]
        assert dependency.name == 'github.com/my/dependency1'
        assert dependency.vcs == 'git'
        assert dependency.url == 'http://localhost:8080/dependency1'
        assert dependency.commit.length() == 40

        assert model.getDependencies('test') == []
    }

    void assertLockIsVendor() {
        GogradleLockModel model = DataExchange.parseYaml(new File(resource, 'gogradle.lock'), GogradleLockModel)
        assert model.getDependencies('build') == [
                [name      : 'github.com/my/dependency1',
                 host      : [name: 'GOGRADLE_ROOT'],
                 vendorPath: 'vendor/github.com/my/dependency1']
        ]

        assert model.getDependencies('test') == []
    }

    @Test
    void 'command line parameter --refresh-dependencies should succeed'() {
        build('installBuildDependencies')
        assert new File(resource, '.gogradle/build_gopath/src/github.com/my/dependency1/commit1.go').exists()
        assert !new File(resource, '.gogradle/build_gopath/src/github.com/my/dependency1/commit2.go').exists()

        GitServer.addFileToRepository(repositories, 'commit2.go')

        build('installBuildDependencies')
        assert stdout.toString().contains('UP-TO-DATE')

        build(['--refresh-dependencies'], 'installBuildDependencies')
        assert new File(resource, '.gogradle/build_gopath/src/github.com/my/dependency1/commit2.go').exists()
    }

    void deleteDependencyTreeCache() {
        new File(resource, '.gogradle/cache/build.bin').delete()
    }

    void deleteDotGogradle() {
        IOUtils.clearDirectory(new File(resource, '.gogradle'))
    }

    void build(List arguments, String... tasks) {
        newBuild({
            it.forTasks(tasks)
        }, arguments)
    }

    void build(String... tasks) {
        build([], tasks)
    }

    @Override
    File getProjectRoot() {
        return resource
    }
}
