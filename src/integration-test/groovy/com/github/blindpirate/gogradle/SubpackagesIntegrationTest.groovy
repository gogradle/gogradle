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
import com.github.blindpirate.gogradle.util.StringUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(GogradleRunner)
@WithResource('')
@WithGitRepos(repoNames = ['vcs', 'sub1', 'sub2'], fileNames = ['vcs.go', 'sub1.go', 'sub2.go'])
@WithMockGo
@WithIsolatedUserhome
class SubpackagesIntegrationTest extends IntegrationTestSupport {

    /*
    localhost/vcs
    |-- sub1
    |    |
    |    \-- sub1.go (import localhost/sub1)
    |
    |-- sub2
    |    |
    |    \-- sub2.go (import localhost/sub2)
    |
    \-- vcs.go

    localhost/sub1
    |
    \-- sub1.go

    localhost/sub2
    |
    \-- sub2.go

    local/a
    |
    |-- a.go
    |
    \-- gogradle.lock (localhost/vcs, subpackages:sub1)

    local/b
    |
    |-- sub
    |    |
    |    \-- sub.go import localhost/vcs
    |
    |-- b.go
    |
    \-- glide.lock (localhost/vcs, subpackages:sub2)

     */

    File repositories

    void addFileToRepo(String repo, String fileInRepo, String content) {
        GitServer.addFileToRepository(new File(repositories, repo), fileInRepo, content)
    }

    @Before
    void setUp() {
        addFileToRepo('vcs', 'sub1/sub1.go', 'package sub1\nimport "localhost/sub1"\nfunc')
        addFileToRepo('vcs', 'sub2/sub2.go', 'package sub2\nimport "localhost/sub2"\nfunc')

        GogradleLockModel model = GogradleLockModel.of([[name       : 'localhost/vcs',
                                                         url        : 'http://localhost:8080/vcs',
                                                         subpackages: ['sub1']]], [])
        IOUtils.write(repositories, 'a/a.go', '')
        IOUtils.write(repositories, 'a/gogradle.lock', DataExchange.toYaml(model))

        IOUtils.write(repositories, 'b/b.go', '')
        IOUtils.write(repositories, 'b/sub/sub.go', 'package pub\nimport "localhost/vcs"\nfunc')


        IOUtils.write(repositories, 'b/glide.lock', '''
imports:
- name: localhost/vcs 
  repo: http://localhost:8080/vcs 
  subpackages:
  - sub2
''')

        writeBuildAndSettingsDotGradle("""
${buildDotGradleBase}
golang {
    packagePath = 'github.com/my/project'
}

repositories {
    golang {
        root ~/localhost\\/\\w+/
        url { path->
            "http://localhost:8080/"+path.split('/')[1]
        }
    }
    golang {
        root ~/local\\/\\w+/
        dir { path ->
            '${StringUtils.toUnixString(repositories)}/'+path.split('/')[1]
        }
    }
}
""")
    }

    @Test
    void "dependency's sub package conflict should be resolved"() {
        IOUtils.append(new File(resource, 'build.gradle'), '''
dependencies {
    golang {
        build 'local/a','local/b'
    }
}
''')
        newBuild {
            it.forTasks('vendor', 'dependencies')
        }

        assertOnlySub1Exists()
        assert stdout.toString().replaceAll(/[0-9a-f]{7}/, '').contains('\\-- localhost/vcs: [sub1]')
        assert stdout.toString().replaceAll(/[0-9a-f]{7}/, '').contains('\\-- localhost/vcs: [sub2, .] ->  [sub1] (*)')
    }

    @Test
    void "local/b's gogradle.lock should be ignored if subpackage specified"() {
        IOUtils.append(new File(resource, 'build.gradle'), '''
dependencies {
    golang {
        build 'local/a'
        build name: 'local/b', subpackages: ['sub'] 
    }
}
''')
        newBuild {
            it.forTasks('vendor', 'dependencies')
        }

        assertOnlySub1Exists()

        assert stdout.toString().replaceAll(/[0-9a-f]{7}/, '').contains('\\-- localhost/vcs: [sub1]')
        assert stdout.toString().replaceAll(/[0-9a-f]{7}/, '').contains('\\-- localhost/vcs: ->  [sub1] (*)')
    }

    @Test
    void "dependency with subpackage's result should be cached"() {
        IOUtils.append(new File(resource, 'build.gradle'), '''
dependencies {
    golang {
        build 'local/a','local/b'
    }
}
''')
        newBuild {
            it.forTasks('lock')
        }

        List buildDeps = DataExchange.parseYaml(new File(resource, 'gogradle.lock'), GogradleLockModel).getDependencies('build')
        assert buildDeps.find { it.name == 'localhost/vcs' }.subpackages == ['sub1']

        newBuild {
            it.forTasks('dependencies', 'vendor')
        }

        assertOnlySub1Exists()

        assert stdout.toString().replaceAll(/[0-9a-f]{7}/, '').contains('\\-- localhost/vcs: [sub1]')

        newBuild({
            it.forTasks('dependencies', 'vendor')
        }, ['--rerun-tasks'])

        assertOnlySub1Exists()

        assert stdout.toString().replaceAll(/[0-9a-f]{7}/, '').contains('\\-- localhost/vcs: [sub1]')
        assert stdout.toString().contains("Resolving cached localhost/vcs")
//
//        assert new File(resource, 'vendor/localhost/vcs/sub1/sub1.go').exists()
//        assert !new File(resource, 'vendor/localhost/vcs/sub2').exists()
//        assert !new File(resource, 'vendor/localhost/vcs/vcs.go').exists()
//
//        assert stdout.toString().replaceAll(/[0-9a-f]{7}/, '').contains('\\-- localhost/vcs: [sub1]')
//        assert stdout.toString().replaceAll(/[0-9a-f]{7}/, '').contains('\\-- localhost/vcs: ->  [sub1] (*)')
    }

    void assertOnlySub1Exists() {
        assert new File(resource, 'vendor/localhost/vcs/sub1/sub1.go').exists()
        assert !new File(resource, 'vendor/localhost/vcs/sub2').exists()
        assert !new File(resource, 'vendor/localhost/vcs/vcs.go').exists()
    }

    @Override
    File getProjectRoot() {
        return resource
    }
}
