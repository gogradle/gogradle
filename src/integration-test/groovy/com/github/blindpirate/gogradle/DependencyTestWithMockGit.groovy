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
import com.github.blindpirate.gogradle.util.IOUtils
import com.github.blindpirate.gogradle.util.StringUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(GogradleRunner)
@WithMockGo
@WithResource('')
@WithGitRepos('git-repo.zip')
@WithIsolatedUserhome
class DependencyTestWithMockGit extends IntegrationTestSupport {
    File resource

    File projectRoot

    File localDependencyRoot

    File repositories

    @Before
    void setUp() {
        localDependencyRoot = new File(resource, 'localDependency')
        IOUtils.write(localDependencyRoot, 'main.go', '')
        IOUtils.write(localDependencyRoot, 'vendor/unrecognized/main.go', '')

        projectRoot = new File(resource, 'projectRoot')

        String buildDotGradle = """
buildscript {
    dependencies {
        classpath files(new File(rootDir, '../../../libs/gogradle-${GogradleGlobal.GOGRADLE_VERSION}-all.jar'))
    }
}
System.setProperty('gradle.user.home','${StringUtils.toUnixString(userhome)}')

apply plugin: 'com.github.blindpirate.gogradle'

golang {
    buildMode = DEVELOP
    packagePath = 'github.com/my/project'
    goVersion = '1.7.1'
    globalCacheFor 0,'second'
    goExecutable = '${StringUtils.toUnixString(goBinPath)}'
}

repositories {
    golang {
        root {it ==~ /github\\.com\\/\\w+\\/\\w+/}
        url {
            def array=it.split('/')
            return 'http://localhost:8080/'+array[1]+'-'+array[2]
        }
    }
}


dependencies {
    golang{
        build 'github.com/firstlevel/a'
        build(
            [name: 'github.com/firstlevel/b', version: '67b0cfae52118d8044c03c1564fd2845ba1b81e1'], // commit3
            'github.com/firstlevel/c@1.0.0'
        )

        build('github.com/firstlevel/d') {
            transitive = false
        }

        build(name: 'github.com/firstlevel/e', commit: '95907c7d') { // commit5
            transitive = true
            exclude name: 'github.com/external/e'
        }

        build name: 'github.com/firstlevel/f', dir: "${StringUtils.toUnixString(localDependencyRoot)}"
        
        test name:'github.com/external/d'
        test name:'github.com/external/a' // this should be excluded since it has already existed in build dependencies
    }
}

"""

        writeBuildAndSettingsDotGradle(buildDotGradle)
    }


    @Test
    void 'resolving dependencies of a complicated package should succeed'() {
        firstBuild()
        secondBuildWithUpToDate()
    }

    void firstBuild() {
        newBuild { build ->
            build.forTasks('vendor', 'dependencies')
        }

        System.out.println(stdout)


        assertDependenciesAre([
                'github.com/firstlevel/a'    : 'commit3',
                'github.com/firstlevel/b'    : 'commit3',
                'github.com/firstlevel/c'    : 'commit3',
                'github.com/firstlevel/d'    : 'commit2',
                'github.com/firstlevel/e'    : 'commit5',

                // vendorexternal/a#1 and vendorexternal/a#2 exist in firstlevel/a#2's dependencies
                // and vendorexternal/a#3 wins because it is in vendor
                'github.com/vendorexternal/a': 'commit1',
                'github.com/vendorexternal/b': 'commit2',

                'github.com/vendoronly/a'    : 'commit2',

                // vendoronly/b#2 is newer
                'github.com/vendoronly/b'    : 'commit2',
                'github.com/vendoronly/c'    : 'commit2',
                'github.com/vendoronly/d'    : 'commit2',
                'github.com/vendoronly/e'    : 'commit2',
                'github.com/external/a'      : 'commit3',
                'github.com/external/b'      : 'commit3',
                'github.com/external/c'      : 'commit4',
                'github.com/external/e'      : 'commit3',

                'github.com/external/d'      : 'commit5'
        ])
    }

    void secondBuildWithUpToDate() {
        newBuild { build ->
            build.forTasks('vendor')
        }
        assert stdout.toString().contains(':resolveBuildDependencies UP-TO-DATE')
        assert stdout.toString().contains(':resolveTestDependencies UP-TO-DATE')
    }

    @Test
    void 'project-level cache should be used in second resolution'() {
        firstBuild()

        IOUtils.forceDelete(new File(projectRoot, '.gogradle/cache/build.bin'))
        IOUtils.forceDelete(new File(projectRoot, '.gogradle/cache/test.bin'))
        repositories.listFiles().each {
            if (!['firstlevel-a', 'firstlevel-c', 'firstlevel-d'].contains(it.name)) {
                IOUtils.forceDelete(it)
            }
        }
        IOUtils.clearDirectory(repositories)

        firstBuild()

        assert !stdout.toString().contains(':resolveBuildDependencies UP-TO-DATE')
        assert !stdout.toString().contains(':resolveTestDependencies UP-TO-DATE')
    }

    @Override
    File getProjectRoot() {
        return projectRoot
    }

    void assertDependenciesAre(Map<String, String> finalDependencies) {
        finalDependencies.each { packageName, commit ->
            assert new File(projectRoot, "vendor/${packageName}/${commit}.go").exists()
        }
    }
    /*
    build:
		github.com/my/project
		|-- github.com/firstlevel/a:1e74619
		|   |-- github.com/external/a:240e90c
		|   |-- github.com/external/e:f3e9fd1
        |   |-- github.com/vendorexternal/a:2f41dbb
        |   \-- github.com/vendorexternal/b:03c8d57
		|-- github.com/firstlevel/b:67b0cfa
		|   |-- github.com/vendoronly/a:github.com/firstlevel/b#67b0cfae52118d8044c03c1564fd2845ba1b81e1/vendor/github.com/vendoronly/a
		|   |   |-- github.com/vendoronly/c:github.com/firstlevel/b#67b0cfae52118d8044c03c1564fd2845ba1b81e1/vendor/github.com/vendoronly/a/vendor/github.com/vendoronly/c
		|   |   |   \-- github.com/vendoronly/b:github.com/firstlevel/b#67b0cfae52118d8044c03c1564fd2845ba1b81e1/vendor/github.com/vendoronly/a/vendor/github.com/vendoronly/c/vendor/github.com/vendoronly/b -> github.com/firstlevel/b#67b0cfae52118d8044c03c1564fd2845ba1b81e1/vendor/github.com/vendoronly/b
		|   |   \-- github.com/vendoronly/d:github.com/firstlevel/b#67b0cfae52118d8044c03c1564fd2845ba1b81e1/vendor/github.com/vendoronly/a/vendor/github.com/vendoronly/d
		|   |       \-- github.com/vendoronly/b:github.com/firstlevel/b#67b0cfae52118d8044c03c1564fd2845ba1b81e1/vendor/github.com/vendoronly/a/vendor/github.com/vendoronly/d/vendor/github.com/vendoronly/b -> github.com/firstlevel/b#67b0cfae52118d8044c03c1564fd2845ba1b81e1/vendor/github.com/vendoronly/b (*)
		|   |-- github.com/vendoronly/b:github.com/firstlevel/b#67b0cfae52118d8044c03c1564fd2845ba1b81e1/vendor/github.com/vendoronly/b (*)
		|   \-- github.com/vendoronly/e:github.com/firstlevel/b#67b0cfae52118d8044c03c1564fd2845ba1b81e1/vendor/github.com/vendoronly/e
		|       \-- github.com/vendoronly/c:github.com/firstlevel/b#67b0cfae52118d8044c03c1564fd2845ba1b81e1/vendor/github.com/vendoronly/e/vendor/github.com/vendoronly/c -> github.com/firstlevel/b#67b0cfae52118d8044c03c1564fd2845ba1b81e1/vendor/github.com/vendoronly/a/vendor/github.com/vendoronly/c (*)
		|-- github.com/firstlevel/c:1.0.0(ccf0636)
		|   |-- github.com/external/a:240e90c (*)
		|   |-- github.com/external/b:dcea2ee
		|   \-- github.com/external/c:9cf45b1
		|-- github.com/firstlevel/d:80aa0f0
		|-- github.com/firstlevel/e:95907c7
		\-- github.com/firstlevel/f:/Users/zhb/Projects/gogradle/build/tmp/resource-e4984884-00ad-4b27-880f-6d55a965b777/localDependency
		    \-- unrecognized:github.com/firstlevel/f@/Users/zhb/Projects/gogradle/build/tmp/resource-e4984884-00ad-4b27-880f-6d55a965b777/localDependency/vendor/unrecognized

	test:
		github.com/my/project
		\-- github.com/external/d:b572b7e
		    \-- github.com/external/e:1340c2c
     */
}
