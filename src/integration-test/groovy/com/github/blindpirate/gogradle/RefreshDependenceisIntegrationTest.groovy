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

import com.github.blindpirate.gogradle.support.*
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.gradle.tooling.BuildException
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(GogradleRunner)
@WithResource('')
@WithMockGo
@WithIsolatedUserhome
@WithGitRepo(repoName = 'a', fileName = '1.go')
class RefreshDependenceisIntegrationTest extends IntegrationTestSupport {
    File repository

    @Before
    void setUp() {
        writeBuildAndSettingsDotGradle("""
${buildDotGradleBase}
golang {
    packagePath = 'github.com/my/package'
    globalCacheFor 0, SECOND
}

dependencies {
    golang {
        build name:'localhost/a', url:'http://localhost:8080/a', tag:'tag1'
    }
}
""")
    }

    @Test
    void 'global cache should be updated when tag not exists'() {
        try {
            newBuild {
                it.forTasks('goVendor')
            }
        } catch (BuildException e) {
            assertOutputContains("Cannot find tag")
        }

        GitServer.addFileToRepository(repository, '2.go', '')

        Repository repository = FileRepositoryBuilder.create(new File(repository, '.git'))
        Git git
        try {
            git = new Git(repository)
            git.tag().setName('tag1').call()
        } finally {
            if (git != null) {
                git.close()
            }
        }

        newBuild {
            it.forTasks('goVendor')
        }

        assert new File(resource, 'vendor/localhost/a/2.go').exists()
    }

    @Override
    File getProjectRoot() {
        return resource
    }
}
