package com.github.blindpirate.gogradle.task;

import groovy.lang.Closure;
import org.gradle.api.Task;
import org.gradle.api.internal.tasks.ContextAwareTaskAction;
import org.gradle.api.internal.tasks.TaskExecutionContext;

import java.util.Map;

public class GoExecutionAction implements ContextAwareTaskAction {
    private Closure closure;
    private Map<String, String> env;

    public static GoExecutionAction wrapClousureWithEnvs(Closure closure, Map<String, String> env) {
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
        Map<String, String> originalEnv = Go.class.cast(task).getCurrentEnv();
        Go.class.cast(task).setCurrentEnv(env);
        try {
            synchronized (task) {
                if (closure.getMaximumNumberOfParameters() == 0) {
                    closure.call();
                } else {
                    closure.call(task);
                }
            }
        } finally {
            Thread.currentThread().setContextClassLoader(original);
            Go.class.cast(task).setCurrentEnv(originalEnv);
        }
    }

    @Override
    public ClassLoader getClassLoader() {
        return closure.getClass().getClassLoader();
    }

}
