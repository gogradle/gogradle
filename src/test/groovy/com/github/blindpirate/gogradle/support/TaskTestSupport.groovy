package com.github.blindpirate.gogradle.support

import org.gradle.api.Task
import org.gradle.api.internal.AbstractTask
import org.gradle.api.internal.project.ProjectInternal

import static com.github.blindpirate.gogradle.util.ReflectionUtils.setFieldSafely

class TaskTestSupport {
    static <T extends Task> T buildTask(ProjectInternal project, Class<T> taskClass, Map context) {
        T ret = AbstractTask.injectIntoNewInstance(project, 'task', taskClass, { taskClass.newInstance() })

        context.each { fieldName, fieldInstance ->
            setFieldSafely(ret, fieldName, fieldInstance)
        }
        return ret
    }

}
