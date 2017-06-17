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

import com.github.blindpirate.gogradle.core.GolangConfigurationManager
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet
import com.github.blindpirate.gogradle.core.dependency.LocalDirectoryDependency
import com.github.blindpirate.gogradle.core.dependency.resolve.DependencyManager
import com.github.blindpirate.gogradle.core.mode.BuildMode
import com.github.blindpirate.gogradle.support.WithProject
import com.github.blindpirate.gogradle.util.ReflectionUtils
import com.github.blindpirate.gogradle.vcs.Git
import com.github.blindpirate.gogradle.vcs.GitMercurialNotationDependency
import com.github.blindpirate.gogradle.vcs.Mercurial
import com.github.blindpirate.gogradle.vcs.VcsAccessor
import com.github.blindpirate.gogradle.vcs.git.GitClientAccessor
import com.github.blindpirate.gogradle.vcs.git.GitDependencyManager
import com.github.blindpirate.gogradle.vcs.git.GolangRepository
import com.github.blindpirate.gogradle.vcs.mercurial.HgClientAccessor
import com.github.blindpirate.gogradle.vcs.mercurial.MercurialDependencyManager
import com.google.inject.Injector
import com.google.inject.Key
import org.gradle.api.Project
import org.gradle.plugins.ide.idea.IdeaPlugin
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

import static com.github.blindpirate.gogradle.core.dependency.AbstractNotationDependency.PropertiesExclusionPredicate
import static com.github.blindpirate.gogradle.task.GolangTaskContainer.*
import static com.github.blindpirate.gogradle.util.DependencyUtils.getExclusionSpecs

@RunWith(GogradleRunner)
@WithProject
class GolangPluginTest {

    Project project

    Injector injector

    @Before
    void applyPlugin() {
        project.pluginManager.apply(GolangPlugin)
        injector = ReflectionUtils.getField(project.getPlugins().getPlugin(GolangPlugin), 'injector')
    }

    @Test
    void 'smoke test should succeed'() {
    }

    @Test
    void 'integration with idea plugin should succeed'() {
        project.pluginManager.apply(IdeaPlugin)
    }

    @Test
    void 'InjectionHelper.INJECTOR_INSTANCE should be assigned'() {
        assert GogradleGlobal.INSTANCE.getInjector() != null
    }

    @Test
    void 'build and test should be added to configurations'() {
        assert project.configurations.build
        assert project.configurations.test
    }

    @Test
    void 'adding a dependency to configuration should succeed'() {
        project.dependencies {
            golang {
                build 'github.com/a/b'
            }
        }

        assert getDependencySet('build').size() == 1

        def dependency = findFirstInDependencies()
        assert dependency.name == 'github.com/a/b'
    }

    private GolangDependencySet getDependencySet(String configurationName) {
        return injector.getInstance(GolangConfigurationManager).getByName(configurationName).dependencies
    }

    @Test
    void 'adding a dependency in form of map should succeed'() {
        project.dependencies {
            golang {
                build name: 'github.com/a/b', commit: 'commitId', tag: '1.0.0', version: 'commitId', vcs: 'git'
            }
        }

        assert getDependencySet('build').size() == 1
        def dependency = findFirstInDependencies()
        assert dependency.name == 'github.com/a/b'
        assert dependency.commit == 'commitId'
        assert dependency.version == 'commitId'
        assert dependency instanceof GitMercurialNotationDependency
    }

    def findFirstInDependencies() {
        return injector.getInstance(GolangConfigurationManager).getByName('build').dependencies.first()
    }

    def findFirstInDependencies(String name) {
        return injector.getInstance(GolangConfigurationManager).getByName('build').dependencies.find {
            it.name == name
        }
    }

    @Test
    void 'DEVELOP and REPRODUCIBLE should be able to be used directly'() {
        project.golang {
            buildMode = project.DEVELOP
        }
        assert injector.getInstance(GolangPluginSetting).buildMode == BuildMode.DEVELOP

        project.golang {
            buildMode = project.REPRODUCIBLE
        }
        assert injector.getInstance(GolangPluginSetting).buildMode == BuildMode.REPRODUCIBLE
    }

    @Test
    void 'adding some dependencies should succeed'() {
        project.dependencies {
            golang {
                build 'github.com/a/b@1.0.0',
                        'github.com/c/d#2.0.0'

                build(
                        [name: 'github.com/e/f', commit: 'commitId'],
                        [name: 'github.com/g/h', commit: 'commitId', vcs: 'git']
                )
            }
        }

        assert getDependencySet('build').size() == 4

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
            golang {
                build name: 'github.com/a/b', dir: project.rootDir.absolutePath
            }
        }

        def dependency = findFirstInDependencies()
        assert dependency.name == 'github.com/a/b'
        assert dependency instanceof LocalDirectoryDependency
    }

    @Test
    void 'adding and configuring a directory dependency should succeed'() {
        project.dependencies {
            golang {
                build(name: 'github.com/a/b', dir: project.rootDir.absolutePath) {
                    transitive = false
                }
            }
        }

        def dependency = findFirstInDependencies()
        assert dependency instanceof LocalDirectoryDependency
        assert !getExclusionSpecs(dependency).isEmpty()
    }

    @Test
    void 'configuring a dependency should succeed'() {
        project.dependencies {
            golang {
                build('github.com/a/b@1.0.0-RELEASE') {
                    transitive = true
                    exclude name: 'github.com/c/d'
                }

                build(name: 'github.com/c/d', url: 'https://github.com/c/d.git') {
                    transitive = false
                }
            }
        }

        def ab = findFirstInDependencies('github.com/a/b')
        assert ab.tag == '1.0.0-RELEASE'
        assert getExclusionSpecs(ab).size() == 1
        assert getExclusionSpecs(ab).first() instanceof PropertiesExclusionPredicate

        def cd = findFirstInDependencies('github.com/c/d')
        assert cd.urls == ['https://github.com/c/d.git']
        assert !getExclusionSpecs(cd).isEmpty()
        // assert !cd.excludeVendor // default value
    }

    @Test
    void 'configuring via params should succeed'() {
        project.dependencies {
            golang {
                build name: 'github.com/a/b', transitive: false
            }
        }

        assert !getExclusionSpecs(findFirstInDependencies()).isEmpty()
    }

    @Test
    void 'configuring repository should succeed'() {
        project.repositories {
            golang {
                root { it.endsWith('b') }
                url 'bbbbb'
            }

            golang {
                root ~/.*d/
                url { name ->
                    'http://' + name
                }
            }
        }

        GolangRepository repository = GogradleGlobal.getInstance(GolangRepositoryHandler).findMatchedRepository('github.com/a/b')

        assert repository.getUrl(null) == 'bbbbb'

        repository = GogradleGlobal.getInstance(GolangRepositoryHandler).findMatchedRepository('github.com/c/d')
        assert repository.getUrl('name') == 'http://name'

        repository = GogradleGlobal.getInstance(GolangRepositoryHandler).findMatchedRepository('something else')
        assert repository.is(GolangRepository.EMPTY_INSTANCE)
    }

    @Test
    void 'getting instance from injector should succeed'() {
        assert GogradleGlobal.getInstance(Key.get(DependencyManager, Git)) instanceof GitDependencyManager
        assert GogradleGlobal.getInstance(Key.get(DependencyManager, Mercurial)) instanceof MercurialDependencyManager
        assert GogradleGlobal.getInstance(Key.get(VcsAccessor, Git)) instanceof GitClientAccessor
        assert GogradleGlobal.getInstance(Key.get(VcsAccessor, Mercurial)) instanceof HgClientAccessor
    }

    @Test
    void 'getting root dir from injector should succeed'() {
        assert GogradleGlobal.getInstance(Project).getProjectDir()
    }

    @Test
    void 'abbr for DEV/REP should succeed'() {
        project.golang {
            buildMode = project.DEV
        }

        assert GogradleGlobal.getInstance(GolangPluginSetting.class).buildMode == BuildMode.DEVELOP
    }

    @Test
    void 'default action should be added after evaluation'() {
        project.getPlugins().getPlugin('com.github.blindpirate.gogradle').afterEvaluate(project)
        [BUILD_TASK_NAME, TEST_TASK_NAME, GOFMT_TASK_NAME].each {
            assert project.getTasksByName(it, false).first().actions.size() == 1
        }
    }
}