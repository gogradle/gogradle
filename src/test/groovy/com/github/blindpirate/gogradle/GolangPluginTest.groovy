package com.github.blindpirate.gogradle

import com.github.blindpirate.gogradle.core.InjectionHelper
import com.github.blindpirate.gogradle.core.dependency.GitDependency
import com.github.blindpirate.gogradle.core.dependency.LocalDirectoryDependency
import org.gradle.api.Project
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(GogradleRunner)
@WithProject
class GolangPluginTest {

    Project project

    @Before
    void applyPlugin() {
        project.pluginManager.apply(GolangPlugin)
    }

    @Test
    void 'smoke test should success'() {
    }

    @Test
    void 'InjectionHelper.INJECTOR_INSTANCE should be assigned'() {
        assert InjectionHelper.INJECTOR_INSTANCE != null
    }

    @Test
    void 'build and test should be added to configurations'() {
        assert project.configurations.build
        assert project.configurations.test
    }

    @Test
    void 'adding a dependency to configuration should success'() {
        project.dependencies {
            build 'github.com/a/b'
        }

        assert project.configurations.build.dependencies.size() == 1

        def dependency = findFirstInDependencies()
        assert dependency.name == 'github.com/a/b'
    }

    @Test
    void 'adding a dependency in form of map should success'() {
        project.dependencies {
            build name: 'github.com/a/b', commit: 'commitId', tag: '1.0.0', version: '1.0.0', vcs: 'git'
        }

        assert project.configurations.build.dependencies.size() == 1;
        def dependency = findFirstInDependencies()
        assert dependency.name == 'github.com/a/b'
        assert dependency.commit == 'commitId'
        assert dependency.version == '1.0.0'
        assert dependency instanceof GitDependency
    }

    def findFirstInDependencies() {
        return project.configurations.build.dependencies.find { true };
    }

    def findFirstInDependencies(String name) {
        return project.configurations.build.dependencies.find { it.name == name }
    }

    @Test
    void 'adding some dependencies should success'() {
        project.dependencies {
            build 'github.com/a/b@1.0.0',
                    'github.com/c/d#2.0.0'

            build(
                    [name: 'github.com/e/f', commit: 'commitId'],
                    [name: 'github.com/g/h', commit: 'commitId', vcs: 'git']
            )
        }

        assert project.configurations.build.dependencies.size() == 4

        def ab = findFirstInDependencies('github.com/a/b')
        assert ab.tag == '1.0.0'
        assert ab.version == '1.0.0'

        def cd = findFirstInDependencies('github.com/c/d')
        assert cd.commit == '2.0.0'

        def ef = findFirstInDependencies('github.com/e/f')
        assert ef.commit == 'commitId'

        def gh = findFirstInDependencies('github.com/g/h')
        assert gh.commit == 'commitId'
    }

    @Test
    void 'adding a directory dependency should success'() {
        project.dependencies {
            build name: 'github.com/a/b', dir: '${GOPATH}/a/b'
        }

        def dependency = findFirstInDependencies()
        assert dependency.name == 'github.com/a/b'
        assert dependency instanceof LocalDirectoryDependency
    }

    @Test
    void 'adding and configuring a directory dependency should success'() {
        project.dependencies {
            build(name: 'github.com/a/b', dir: '${GOPATH}/a/b') {
                transitive = false
            }
        }

        def dependency = findFirstInDependencies()
        assert dependency instanceof LocalDirectoryDependency
        assert !dependency.transitive

    }

    @Test
    void 'configuring a dependency should success'() {
        project.dependencies {
            build('github.com/a/b@1.0.0-RELEASE') {
                transitive = true
                // TODO should we support this feature?
                // excludeVendor = true
                exclude module: 'github.com/c/d'
            }

            build(name: 'github.com/c/d', url: 'git@github.com:a/b.git') {
                transitive = false
            }
        }

        def ab = findFirstInDependencies('github.com/a/b')
        assert ab.tag == '1.0.0-RELEASE'
        assert ab.transitive
        //assert ab.excludeVendor
        assert ab.excludes.containsValue('github.com/c/d')

        def cd = findFirstInDependencies('github.com/c/d')
        assert cd.url == 'git@github.com:a/b.git'
        assert !cd.transitive
        // assert !cd.excludeVendor // default value


    }

    @Test
    void 'configuring via params should success'() {
        project.dependencies {
            build name: 'github.com/a/b', transitive: false
        }

        assert !(findFirstInDependencies().transitive)
    }

}