package com.github.blindpirate.gogradle

import com.github.blindpirate.gogradle.core.InjectionHelper
import com.github.blindpirate.gogradle.core.pack.LocalDirectoryDependency
import com.github.blindpirate.gogradle.vcs.git.GitNotationDependency
import org.gradle.api.Project
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

import static com.github.blindpirate.gogradle.core.dependency.AbstractGolangDependency.PropertiesExclusionSpec
import static com.github.blindpirate.gogradle.util.DependencyUtils.getExclusionSpecs

@RunWith(GogradleRunner)
@WithProject
class GolangPluginTest {

    Project project

    @Before
    void applyPlugin() {
        project.pluginManager.apply(GolangPlugin)
    }

    @Test
    void 'smoke test should succeed'() {
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
    void 'adding a dependency to configuration should succeed'() {
        project.dependencies {
            build 'github.com/a/b'
        }

        assert project.configurations.build.dependencies.size() == 1

        def dependency = findFirstInDependencies()
        assert dependency.name == 'github.com/a/b'
    }

    @Test
    void 'adding a dependency in form of map should succeed'() {
        project.dependencies {
            build name: 'github.com/a/b', commit: 'commitId', tag: '1.0.0', version: 'commitId', vcs: 'git'
        }

        assert project.configurations.build.dependencies.size() == 1
        def dependency = findFirstInDependencies()
        assert dependency.name == 'github.com/a/b'
        assert dependency.commit == 'commitId'
        assert dependency.version == 'commitId'
        assert dependency instanceof GitNotationDependency
    }

    def findFirstInDependencies() {
        return project.configurations.build.dependencies.find { true }
    }

    def findFirstInDependencies(String name) {
        return project.configurations.build.dependencies.find { it.name == name }
    }

    @Test
    void 'adding some dependencies should succeed'() {
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
        assert !ab.version

        def cd = findFirstInDependencies('github.com/c/d')
        assert cd.commit == '2.0.0'

        def ef = findFirstInDependencies('github.com/e/f')
        assert ef.commit == 'commitId'

        def gh = findFirstInDependencies('github.com/g/h')
        assert gh.commit == 'commitId'
    }

    @Test
    void 'adding a directory dependency should succeed'() {
        project.dependencies {
            build name: 'github.com/a/b', dir: project.rootDir.absolutePath
        }

        def dependency = findFirstInDependencies()
        assert dependency.name == 'github.com/a/b'
        assert dependency instanceof LocalDirectoryDependency
    }

    @Test
    void 'adding and configuring a directory dependency should succeed'() {
        project.dependencies {
            build(name: 'github.com/a/b', dir: project.rootDir.absolutePath) {
                transitive = false
            }
        }

        def dependency = findFirstInDependencies()
        assert dependency instanceof LocalDirectoryDependency
        assert !getExclusionSpecs(dependency).isEmpty()

    }

    @Test
    void 'configuring a dependency should succeed'() {
        project.dependencies {
            build('github.com/a/b@1.0.0-RELEASE') {
                transitive = true
                exclude name: 'github.com/c/d'
            }

            build(name: 'github.com/c/d', url: 'https://github.com/a/b.git') {
                transitive = false
            }
        }

        def ab = findFirstInDependencies('github.com/a/b')
        assert ab.tag == '1.0.0-RELEASE'
        assert getExclusionSpecs(ab).size() == 1
        assert getExclusionSpecs(ab).first() instanceof PropertiesExclusionSpec

        def cd = findFirstInDependencies('github.com/c/d')
        assert cd.url == 'https://github.com/c/d.git'
        assert !getExclusionSpecs(cd).isEmpty()
        // assert !cd.excludeVendor // default value
    }

    @Test
    void 'configuring via params should succeed'() {
        project.dependencies {
            build name: 'github.com/a/b', transitive: false
        }

        assert !getExclusionSpecs(findFirstInDependencies()).isEmpty()
    }

}