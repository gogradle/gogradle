package com.github.blindpirate.gogradle.dependencytest

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.WithProject
import com.github.blindpirate.gogradle.WithResource
import com.github.blindpirate.gogradle.support.IntegrationTestSupport
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

import java.nio.file.Path

import static com.github.blindpirate.gogradle.build.BuildManager.BUILD_GOPATH
import static com.github.blindpirate.gogradle.build.BuildManager.GOGRADLE_BUILD_DIR

@RunWith(GogradleRunner)
@WithProject
@WithResource('dependency-test-with-mock-git.zip')
class DependencyTestWithMockGit extends IntegrationTestSupport {

    File globalCache

    File fsRoot

    File mockGitRepo

    @Before
    void setUp() {
//        resource = new File(getClass().getClassLoader().getResource('build-with-mock-git').toURI())
        globalCache = resource.toPath().resolve('global-cache').toFile()
        projectRoot = resource.toPath().resolve('project').toFile()
        fsRoot = resource.toPath().resolve('mock-fs-root').toFile()
        mockGitRepo = resource.toPath().resolve('mock-git-repo').toFile()

        baseSetUp()
    }


    @Test
    void 'resolving dependencies of a complicated package should success'() {
        // given
        newBuild { build ->
            build.forTasks('installDependencies')
//            build.forTasks('dependencies')
        }

//        println(stdout)

        // then
        assertDependenciesAre([
                'github.com/firstlevel/a'    : 'commit2',
                'github.com/firstlevel/b'    : 'commit3',
                'github.com/firstlevel/c'    : 'commit3',
                'github.com/firstlevel/d'    : 'commit2',
                'github.com/firstlevel/e'    : 'commit5',

                // vendorexternal/a#1 and vendorexternal/a#2 exist in firstlevel/a#2's dependencies
                // and vendorexternal/a#1 wins because it is in vendor directory
                // even though vendorexternal/a#2 is newer
                'github.com/vendorexternal/a': 'commit1',
                'github.com/vendorexternal/b': 'commit2',

                'github.com/vendoronly/a'    : 'commit2',

                // vendoronly/b#2 is newer
                'github.com/vendoronly/b'    : 'commit2',
                'github.com/vendoronly/c'    : 'commit2',
                'github.com/vendoronly/d'    : 'commit2',
                'github.com/vendoronly/e'    : 'commit2',
                'github.com/external/a'      : 'commit3',
                'github.com/external/b'      : 'commit4',
                'github.com/external/c'      : 'commit4',
                'github.com/external/d'      : 'commit4',
                'github.com/external/e'      : 'commit3',

        ])
    }

    @Override
    List<String> buildArguments() {
        return super.buildArguments() + ["-PmockFsRoot=${fsRoot.absolutePath}",
                                         "-PmockGitRepo=${mockGitRepo.absolutePath}",
                                         "-Puserhome=${userhome.absolutePath}"]
    }

    void assertDependenciesAre(Map<String, String> finalDependencies) {
        Path projectGopath = projectRoot.toPath().resolve(GOGRADLE_BUILD_DIR).resolve(BUILD_GOPATH)

        finalDependencies.each { packageName, commit ->
            assert projectGopath.resolve(packageName).resolve(commit + '.go').toFile().exists()
        }
    }
}
