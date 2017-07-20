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
import org.gradle.tooling.BuildException
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

import static com.github.blindpirate.gogradle.util.StringUtils.toUnixString

@RunWith(GogradleRunner)
@WithIsolatedUserhome
@WithMockGo
@WithResource('')
@WithGitRepo(repoName = 'a', fileName = 'a.go')
class ResolutionStackIntegrationTest extends IntegrationTestSupport {
    File repository

    @Before
    void setUp() {
        GitServer.addFileToRepository(repository, 'a.go', '''
package main
import "unrecognized"
func main
''')
        GogradleLockModel model = GogradleLockModel.of([[name: 'localhost/a', url: 'http://localhost:8080/a']], [])
        IOUtils.write(repository, 'b/gogradle.lock', DataExchange.toYaml(model))

        writeBuildAndSettingsDotGradle("""
${buildDotGradleBase}
golang {
    packagePath = 'github.com/my/package'
}
dependencies {
    golang {
        build name:'local/b',dir:'${toUnixString(new File(repository, 'b'))}'
    }
}
""")
    }

    @Test
    void 'resolution stack should be printed'() {
        try {
            newBuild {
                it.forTasks('vendor')
            }
        } catch (BuildException e) {
            assert stderr.toString().replaceAll(/[a-f0-9]{40}/, '').contains("""
Cannot recognized package: unrecognized
Resolution stack is:
+- github.com/my/package
 +- local/b@${toUnixString(repository)}/b
  +- localhost/a#""")
        }
    }

    @Test
    void 'resolution stack should be printed when resolving vendor failed'() {
        GogradleLockModel model =
                GogradleLockModel.of([[name      : 'vendordependency/c',
                                       vendorPath: 'vendor/vendordependency/c',
                                       host      : [name: 'localhost/a', url: 'http://localhost:8080/a', commit: 'unexistent'],
                                       transitive: false
                                      ]], [])
        IOUtils.write(resource, 'gogradle.lock', DataExchange.toYaml(model))

        try {
            newBuild {
                it.forTasks('vendor')
            }
        } catch (BuildException e) {
            assert stderr.toString().contains("""\
Cannot resolve dependency:GitNotationDependency{name='localhost/a', commit='unexistent', urls='[http://localhost:8080/a]'}
Resolution stack is:
+- github.com/my/package
""")
        }
    }


    @Override
    File getProjectRoot() {
        return resource
    }
}
