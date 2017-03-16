package com.github.blindpirate.gogradle.task

import com.github.blindpirate.gogradle.GolangPluginSetting
import com.github.blindpirate.gogradle.build.BuildManager
import com.github.blindpirate.gogradle.core.BuildConstraintManager
import com.github.blindpirate.gogradle.core.dependency.lock.LockedDependencyManager
import com.github.blindpirate.gogradle.core.dependency.produce.DependencyVisitor
import com.github.blindpirate.gogradle.core.dependency.produce.strategy.GogradleRootProduceStrategy
import com.github.blindpirate.gogradle.core.dependency.tree.DependencyTreeFactory
import com.github.blindpirate.gogradle.crossplatform.GoBinaryManager
import com.github.blindpirate.gogradle.ide.IntellijIdeIntegration
import com.github.blindpirate.gogradle.ide.IdeaIntegration
import com.github.blindpirate.gogradle.util.ReflectionUtils
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
    BuildManager buildManager
    @Mock
    GoBinaryManager goBinaryManager
    @Mock
    BuildConstraintManager buildConstraintManager
    @Mock
    LockedDependencyManager lockedDependencyManager
    @Mock
    IdeaIntegration ideaIntegration
    @Mock
    IntellijIdeIntegration intellijIdeIntegration
    // This is a real task container for test tasks to fetch notationDependency tasks from
    GolangTaskContainer golangTaskContainer = new GolangTaskContainer()

    @Before
    void superSetUp() {
        for (Map.Entry entry in GolangTaskContainer.TASKS.entrySet())
            GolangTaskContainer.TASKS.each { taskName, taskClass ->
                golangTaskContainer.put(taskClass, Mockito.mock(taskClass))
            }
    }

    void assertTaskDependsOn(Task task, Object dependency) {
        def dependencies = ReflectionUtils.getField(task, 'dependencies')
        Set<Object> values = ReflectionUtils.getField(dependencies, 'values')
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
                      ideaIntegration        : ideaIntegration,
                      intellijIdeIntegration : intellijIdeIntegration]

        T ret = AbstractTask.injectIntoNewInstance(project, 'task', taskClass, { taskClass.newInstance() })

        fields.each { fieldName, fieldInstance ->
            setFieldSafely(ret, fieldName, fieldInstance)
        }

        return ret
    }

}
