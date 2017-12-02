/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.github.blindpirate.gogradle

import com.github.blindpirate.gogradle.core.dependency.lock.GogradleLockModel
import com.github.blindpirate.gogradle.support.*
import com.github.blindpirate.gogradle.util.DataExchange
import com.github.blindpirate.gogradle.util.IOUtils
import com.github.blindpirate.gogradle.util.ProcessUtils
import com.github.blindpirate.gogradle.vcs.git.GitClientAccessor
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(GogradleRunner)
@WithResource('')
@WithIsolatedUserhome
@WithGitRepos(repoNames = ['a1', 'a2', 'b1', 'b2'], fileNames = ['a1.go', 'a2.go', 'b1.go', 'b2.go'])
class LockIntegrationTest extends IntegrationTestSupport {
    File repositories

    GitClientAccessor gitClientAccessor = new GitClientAccessor(new ProcessUtils())

    String headCommitOfA1

    @Before
    void setUp() {
        writeBuildAndSettingsDotGradle("""
${buildDotGradleBase}
golang {
    packagePath='github.com/my/package'
}

dependencies {
    golang {
        build name:'github.com/my/a',url:'http://localhost:8080/a2'
        test name:'github.com/my/b',url:'http://localhost:8080/b1'
    }
}
""")
        headCommitOfA1 = gitClientAccessor.headCommitOfBranch(new File(repositories, 'a1'), 'master').id
        GogradleLockModel model = GogradleLockModel.of([[name: 'github.com/my/a', 'url': 'http://localhost:8080/a1', version: headCommitOfA1]],
                [])
        IOUtils.write(new File(resource, 'gogradle.lock'), DataExchange.toYaml(model))
    }

    @Test
    void 'dependency in build.gradle should be used in DEVELOP'() {
        IOUtils.append(new File(resource, 'build.gradle'), 'golang {buildMode=DEV}')
        build('lock', 'vendor')
        assert new File(resource, 'vendor/github.com/my/a/a2.go').exists()
        assert new File(resource, 'vendor/github.com/my/b/b1.go').exists()

        Map a = getLockedDependency('build')[0]
        assert a.name == 'github.com/my/a'
        assert a.vcs == 'git'
        assert a.url == 'http://localhost:8080/a2'
        assert a.commit.length() == 40

        Map b = getLockedDependency('test')[0]
        assert b.name == 'github.com/my/b'
        assert b.vcs == 'git'
        assert b.url == 'http://localhost:8080/b1'
        assert b.commit.length() == 40


    }

    @Test
    void 'locked dependency should be used in REPRODUCIBLE'() {
        build('lock', 'vendor')
        assert new File(resource, 'vendor/github.com/my/a/a1.go').exists()

        Map a = getLockedDependency('build')[0]
        assert getLockedDependency('test').isEmpty()

        assert a.name == 'github.com/my/a'
        assert a.vcs == 'git'
        assert a.url == 'http://localhost:8080/a1'
        assert a.commit == headCommitOfA1
    }

    @Test
    void 'persistent cache should be used'() {
        build('vendor')

        deleteDependencyTreeCache()
        build('vendor')
        assert new File(resource, 'vendor/github.com/my/a/a1.go').exists()
        assert stdout.toString().contains('Resolving cached')
    }

    List getLockedDependency(String configuration) {
        GogradleLockModel model = DataExchange.parseYaml(new File(resource, 'gogradle.lock'), GogradleLockModel)
        return model.getDependencies(configuration)
    }

    @Test
    void 'command line parameter --refresh-dependencies should succeed'() {
        IOUtils.append(new File(resource, 'build.gradle'), 'golang {buildMode=DEV}\n')

        build('vendor')
        assert new File(resource, 'vendor/github.com/my/a/a2.go').exists()

        GitServer.addFileToRepository(new File(repositories, 'a2'), 'commit2.go', '')

        build('vendor')
        assert stdout.toString().contains('UP-TO-DATE')

        IOUtils.append(new File(resource, 'build.gradle'), 'golang { globalCacheFor(0, MINUTE) }')
        build(['--refresh-dependencies'], 'vendor')
        assert new File(resource, 'vendor/github.com/my/a/commit2.go').exists()
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
