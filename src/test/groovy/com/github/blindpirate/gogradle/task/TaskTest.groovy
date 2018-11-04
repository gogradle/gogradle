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

package com.github.blindpirate.gogradle.task

import com.github.blindpirate.gogradle.GogradleGlobal
import com.github.blindpirate.gogradle.GolangPlugin
import com.github.blindpirate.gogradle.GolangPluginSetting
import com.github.blindpirate.gogradle.build.BuildManager
import com.github.blindpirate.gogradle.core.BuildConstraintManager
import com.github.blindpirate.gogradle.core.GolangConfigurationManager
import com.github.blindpirate.gogradle.core.cache.ProjectCacheManager
import com.github.blindpirate.gogradle.core.cache.VendorSnapshoter
import com.github.blindpirate.gogradle.core.dependency.GogradleRootProject
import com.github.blindpirate.gogradle.core.dependency.lock.LockedDependencyManager
import com.github.blindpirate.gogradle.core.dependency.produce.DependencyVisitor
import com.github.blindpirate.gogradle.core.dependency.produce.strategy.GogradleRootProduceStrategy
import com.github.blindpirate.gogradle.core.dependency.tree.DependencyTreeFactory
import com.github.blindpirate.gogradle.crossplatform.GoBinaryManager
import com.github.blindpirate.gogradle.support.WithMockInjector
import com.github.blindpirate.gogradle.util.ReflectionUtils
import org.gradle.api.Task
import org.gradle.api.internal.AbstractTask
import org.gradle.api.internal.GradleInternal
import org.gradle.api.internal.plugins.ExtensionContainerInternal
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.internal.project.taskfactory.TaskIdentity
import org.gradle.internal.service.ServiceRegistry
import org.gradle.util.Path
import org.junit.Before
import org.mockito.Mock

import static com.github.blindpirate.gogradle.util.ReflectionUtils.setFieldSafely
import static org.mockito.ArgumentMatchers.anyString
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

@WithMockInjector
abstract class TaskTest {
    @Mock
    ProjectInternal project
    @Mock
    GogradleRootProduceStrategy strategy
    @Mock
    DependencyVisitor visitor
    @Mock
    GolangPluginSetting setting
    @Mock
    DependencyTreeFactory dependencyTreeFactory
    @Mock
    BuildManager buildManager
    @Mock
    GoBinaryManager goBinaryManager
    @Mock
    BuildConstraintManager buildConstraintManager
    @Mock
    LockedDependencyManager lockedDependencyManager
    @Mock
    GolangConfigurationManager configurationManager
    @Mock
    ProjectCacheManager projectCacheManager
    @Mock
    VendorSnapshoter vendorSnapshoter
    @Mock
    GogradleRootProject gogradleRootProject
    @Mock
    ExtensionContainerInternal extensionContainer
    @Mock
    ServiceRegistry serviceRegistry
    @Mock
    GradleInternal gradle
    // This is a real task container for test tasks to fetch notationDependency tasks from
    GolangTaskContainer golangTaskContainer = new GolangTaskContainer()

    @Before
    void superSetUp() {
        for (Map.Entry entry in GolangTaskContainer.TASKS.entrySet()) {
            GolangTaskContainer.TASKS.each { taskName, taskClass ->
                golangTaskContainer.put(taskClass, mock(taskClass))
            }
        }
        when(GogradleGlobal.INSTANCE.injector.getInstance(BuildManager)).thenReturn(buildManager)
        when(project.getProjectPath()).thenReturn(mock(Path))
        when(project.getIdentityPath()).thenReturn(mock(Path))
    }

    void assertTaskDependsOn(Task task, Object dependency) {
        def dependencies = task.getTaskDependencies()
        Set<Object> values = ReflectionUtils.getField(dependencies, 'mutableValues')
        assert values.contains(dependency)
    }

    protected <T> T buildTask(Class<T> taskClass) {
        Map fields = [setting                : setting,
                      dependencyTreeFactory  : dependencyTreeFactory,
                      strategy               : strategy,
                      visitor                : visitor,
                      golangTaskContainer    : golangTaskContainer,
                      buildManager           : buildManager,
                      goBinaryManager        : goBinaryManager,
                      buildConstraintManager : buildConstraintManager,
                      lockedDependencyManager: lockedDependencyManager,
                      configurationManager   : configurationManager,
                      projectCacheManager    : projectCacheManager,
                      gogradleRootProject    : gogradleRootProject,
                      vendorSnapshoter       : vendorSnapshoter]

        when(project.getExtensions()).thenReturn(extensionContainer)
        when(project.getServices()).thenReturn(serviceRegistry)

        Path path = Path.path(taskClass.simpleName.toLowerCase())

        when(project.projectPath(anyString())).thenReturn(path)
        when(project.identityPath(anyString())).thenReturn(path)
        when(project.getGradle()).thenReturn(gradle)
        when(gradle.getIdentityPath()).thenReturn(Path.path('gradle'))
        when(extensionContainer.getByName(GolangPlugin.GOGRADLE_INJECTOR)).thenReturn(GogradleGlobal.INSTANCE.injector)
        T ret = AbstractTask.injectIntoNewInstance(project, TaskIdentity.create(taskClass.simpleName.toLowerCase(), taskClass, project), { taskClass.newInstance() })

        fields.each { fieldName, fieldInstance ->
            setFieldSafely(ret, fieldName, fieldInstance)
        }

        return ret
    }

}
