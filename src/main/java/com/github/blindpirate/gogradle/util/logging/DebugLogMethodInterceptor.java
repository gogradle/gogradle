package com.github.blindpirate.gogradle.util.logging;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.lang.reflect.Method;

public class DebugLogMethodInterceptor implements MethodInterceptor {
    private static final Logger LOGGER = Logging.getLogger(DebugLogMethodInterceptor.class);

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        beforeMethod(methodInvocation);
        long startTime = System.nanoTime();
        Throwable exception = null;
        try {
            return methodInvocation.proceed();
        } catch (Throwable e) {
            exception = e;
            throw e;
        } finally {
            long endTime = System.nanoTime();
            afterMethod(methodInvocation, exception, endTime - startTime);
        }
    }

    private void afterMethod(MethodInvocation methodInvocation, Throwable e, long nanoseconds) {
        if (!LOGGER.isDebugEnabled()) {
            return;
        }
        Object target = methodInvocation.getThis();
        Method method = methodInvocation.getMethod();
        if (e == null) {
            LOGGER.debug("Exiting method {} of class {}, total time is {} ms",
                    method.getName(),
                    target.getClass().getSimpleName(),
                    toString(nanoseconds));
        } else {
            LOGGER.debug("Exiting method {} of class {}, total time is {} ms, exception is {}",
                    method.getName(),
                    target.getClass().getSimpleName(),
                    toString(nanoseconds),
                    toString(e));
        }
    }

    private String toString(Throwable e) {
        return "[" + e.getClass().getSimpleName() + "]" + e.getMessage();
    }

    private String toString(long nanoseconds) {
        return String.format("%.3f", nanoseconds / 1000_000d);
    }

    private void beforeMethod(MethodInvocation methodInvocation) {
        if (!LOGGER.isDebugEnabled()) {
            return;
        }
        Object target = methodInvocation.getThis();
        Object[] arguments = methodInvocation.getArguments();
        Method method = methodInvocation.getMethod();
        LOGGER.debug("Entering method {} of class {}, the arguments is {}",
                method.getName(), target.getClass().getSimpleName(),
                toString(arguments));
    }

    private String toString(Object[] arguments) {
        StringBuilder sb = new StringBuilder("[");
        for (Object argument : arguments) {
            sb.append(argument);
        }
        sb.append("]");
        return sb.toString();
    }
}
