package com.github.blindpirate.gogradle.task

import com.github.blindpirate.gogradle.GolangPluginSetting
import com.github.blindpirate.gogradle.build.BuildManager
import com.github.blindpirate.gogradle.core.dependency.produce.DependencyVisitor
import com.github.blindpirate.gogradle.core.dependency.produce.strategy.GogradleRootProduceStrategy
import com.github.blindpirate.gogradle.core.dependency.tree.DependencyTreeFactory
import com.github.blindpirate.gogradle.support.TaskTestSupport
import org.gradle.api.internal.project.ProjectInternal
import org.junit.Before
import org.mockito.Mock
import org.mockito.Mockito

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
    // This is a real task container for test tasks to fetch notationDependency tasks from
    GolangTaskContainer golangTaskContainer = new GolangTaskContainer()

    @Before
    void superSetUp() {
        for (Map.Entry entry in GolangTaskContainer.TASKS.entrySet())
            GolangTaskContainer.TASKS.each { taskName, taskClass ->
                golangTaskContainer.put(taskClass, Mockito.mock(taskClass))
            }
    }

    protected <T> T buildTask(Class<T> taskClass) {
        Map fields = [setting              : setting,
                      dependencyTreeFactory: dependencyTreeFactory,
                      strategy             : strategy,
                      visitor              : visitor,
                      golangTaskContainer  : golangTaskContainer,
                      buildManager         : buildManager]
        return TaskTestSupport.buildTask(project, taskClass, fields)
    }

}
