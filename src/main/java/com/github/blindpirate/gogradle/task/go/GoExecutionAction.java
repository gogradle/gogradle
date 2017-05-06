package com.github.blindpirate.gogradle.task.go;

import com.github.blindpirate.gogradle.Go;
import groovy.lang.Closure;
import org.gradle.api.Task;
import org.gradle.api.internal.tasks.ContextAwareTaskAction;
import org.gradle.api.internal.tasks.TaskExecutionContext;

import java.util.Map;

public class GoExecutionAction implements ContextAwareTaskAction {
    private Closure closure;
    private Map<String, String> env;

    public static GoExecutionAction wrapClosureWithEnvs(Closure closure, Map<String, String> env) {
        GoExecutionAction ret = new GoExecutionAction();
        ret.env = env;
        ret.closure = closure;
        return ret;
    }

    private GoExecutionAction() {
    }

    @Override
    public void contextualise(TaskExecutionContext context) {
    }

    @Override
    public void execute(Task task) {
        closure.setDelegate(task);
        closure.setResolveStrategy(Closure.DELEGATE_FIRST);
        ClassLoader original = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(closure.getClass().getClassLoader());
        Map<String, String> originalEnv = Go.class.cast(task).getSingleBuildEnvironment();
        Go.class.cast(task).setSingleBuildEnvironment(env);
        try {
            synchronized (task) {
                closure.call(task);
            }
        } finally {
            Thread.currentThread().setContextClassLoader(original);
            Go.class.cast(task).setSingleBuildEnvironment(originalEnv);
        }
    }

    @Override
    public ClassLoader getClassLoader() {
        return closure.getClass().getClassLoader();
    }

}
