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

package com.github.blindpirate.gogradle.util.logging;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.lang.reflect.Method;
import java.util.Arrays;

public class DebugLogMethodInterceptor implements MethodInterceptor {
    private static final Logger LOGGER = Logging.getLogger(DebugLogMethodInterceptor.class);

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        beforeMethod(methodInvocation);
        long startTime = System.nanoTime();
        Throwable exception = null;
        Object ret = null;
        try {
            ret = methodInvocation.proceed();
            return ret;
        } catch (Throwable e) {
            exception = e;
            throw e;
        } finally {
            long endTime = System.nanoTime();
            afterMethod(methodInvocation, ret, exception, endTime - startTime);
        }
    }

    private void afterMethod(MethodInvocation methodInvocation, Object returnValue, Throwable e, long nanoseconds) {
        if (!LOGGER.isDebugEnabled()) {
            return;
        }
        Object target = methodInvocation.getThis();
        Method method = methodInvocation.getMethod();
        if (e == null) {
            LOGGER.debug("Exiting method {} of class {}, total time is {} ms, return {}",
                    method.getName(),
                    target.getClass().getSimpleName(),
                    toString(nanoseconds),
                    returnValue);
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

    @SuppressWarnings({"checkstyle:magicnumber"})
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
        Arrays.stream(arguments).forEach(arg -> sb.append(arg));
        sb.append("]");
        return sb.toString();
    }
}
