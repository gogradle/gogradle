package com.github.blindpirate.gogradle.util;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.internal.TaskInternal;

public class TaskUtil {
    public static void runTask(Project project, String taskName) {
        Task task = project.getTasks().getByName(taskName);
        TaskInternal.class.cast(task).execute();
    }
}
