package com.github.blindpirate.gogradle.task

import com.github.blindpirate.gogradle.GolangPluginSetting
import com.github.blindpirate.gogradle.core.GolangTaskContainer
import com.github.blindpirate.gogradle.core.dependency.DependencyInstaller
import com.github.blindpirate.gogradle.core.dependency.produce.DependencyVisitor
import com.github.blindpirate.gogradle.core.dependency.produce.strategy.GogradleRootProduceStrategy
import com.github.blindpirate.gogradle.core.dependency.tree.DependencyTreeFactory
import org.gradle.api.Task
import org.gradle.api.internal.AbstractTask
import org.gradle.api.internal.project.ProjectInternal
import org.junit.Before
import org.mockito.Mock
import org.mockito.Mockito

import static com.github.blindpirate.gogradle.util.ReflectionUtils.setFieldSafely

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
    DependencyInstaller dependencyInstaller
    // This is a real task container for test tasks to fetch notationDependency tasks from
    GolangTaskContainer golangTaskContainer = new GolangTaskContainer()

    @Before
    void superSetUp() {
        GolangTaskContainer.TASKS.each { taskName, taskClass ->
            golangTaskContainer.put(taskClass, Mockito.mock(taskClass))
        }
    }

    protected void injectTaskMembers(Task task) {
        setFieldSafely(task, 'setting', setting)
        setFieldSafely(task, 'dependencyTreeFactory', dependencyTreeFactory)
        setFieldSafely(task, 'strategy', strategy)
        setFieldSafely(task, 'visitor', visitor)
        setFieldSafely(task, 'dependencyInstaller', dependencyInstaller)
        setFieldSafely(task, 'golangTaskContainer', golangTaskContainer)
    }

    protected <T> T buildTask(Class<T> taskClass) {
        T ret = AbstractTask.injectIntoNewInstance(project, 'task', taskClass, { taskClass.newInstance() })
        injectTaskMembers(ret)
        return ret
    }

}
