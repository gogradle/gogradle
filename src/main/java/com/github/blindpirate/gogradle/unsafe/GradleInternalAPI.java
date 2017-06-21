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

package com.github.blindpirate.gogradle.unsafe;

import com.github.blindpirate.gogradle.task.go.GoTestResultsProvider;
import com.github.blindpirate.gogradle.util.ExceptionHandler;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.gradle.api.Action;
import org.gradle.api.internal.tasks.testing.junit.report.DefaultTestReport;
import org.gradle.api.internal.tasks.testing.junit.result.TestClassResult;
import org.gradle.api.invocation.Gradle;
import org.gradle.internal.operations.BuildOperationQueue;
import org.gradle.internal.operations.MultipleBuildOperationFailures;
import org.gradle.internal.operations.RunnableBuildOperation;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static org.joor.Reflect.on;

/**
 * Very ugly hack for Gradle internal API.
 */
@SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_INFERRED")
public class GradleInternalAPI {
    public static void renderTestReport(List<TestClassResult> testResults, File outputDir) {
        GoTestResultsProvider provider = new GoTestResultsProvider(testResults);
        try {
            String executorClassName = isGradle4()
                    ? "org.gradle.internal.operations.BuildOperationExecutor"
                    : "org.gradle.internal.operations.BuildOperationProcessor";

            Class buildOperationProcessorClass = Class.forName(executorClassName);
            Constructor<DefaultTestReport> constructor =
                    DefaultTestReport.class.getConstructor(buildOperationProcessorClass);
            Object buildOperationExecutor = Proxy.newProxyInstance(Gradle.class.getClassLoader(),
                    new Class[]{buildOperationProcessorClass},
                    new BuildOperationExecutorOrProcessor());
            constructor.newInstance(buildOperationExecutor).generateReport(provider, outputDir);
        } catch (ReflectiveOperationException e) {
            throw ExceptionHandler.uncheckException(e);
        }
    }

    private static boolean isGradle4() {
        return "4".equals(System.getProperty("GRADLE_MAJOR_VERSION"));
    }

    private static class BuildOperationExecutorOrProcessor implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if ("run".equals(method.getName()) && args[0] instanceof Action) {
                // Gradle 3.x
                // org.gradle.internal.operations.BuildOperationProcessor.run
                runAction(args);
            } else if ("runAll".equals(method.getName()) && args.length == 1) {
                // Gradle 4.0
                // org.gradle.internal.operations.BuildOperationExecutor.runAll(RunnableBuildOperation)
                runAction(args);
            } else {
                throw new UnsupportedOperationException();
            }

            return null;
        }

        @SuppressWarnings("unchecked")
        private void runAction(Object[] args) {
            BuildOperationQueue queue = new SerialBuildOperationQueue();
            Action.class.cast(args[0]).execute(queue);
            queue.waitForCompletion();
        }
    }

    private static class SerialBuildOperationQueue<T extends RunnableBuildOperation> implements BuildOperationQueue<T> {
        private Queue<RunnableBuildOperation> queue = new LinkedList<>();

        @Override
        public void add(RunnableBuildOperation operation) {
            queue.add(operation);
        }

        @Override
        public void cancel() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void waitForCompletion() throws MultipleBuildOperationFailures {
            try {
                for (RunnableBuildOperation operation : queue) {
                    if (isGradle4()) {
                        Class contextClass = Class.forName("org.gradle.internal.operations.BuildOperationContext");
                        Object context = on(new DefaultBuildOperationContext()).as(contextClass);
                        on(operation).call("run", context);
                    } else {
                        on(operation).call("run");
                    }
                }
            } catch (ReflectiveOperationException e) {
                throw ExceptionHandler.uncheckException(e);
            }
        }

        @Override
        public void setLogLocation(String logLocation) {
            throw new UnsupportedOperationException();
        }
    }


    // Gradle 4.0
    // org.gradle.internal.operations.BuildOperationContext
    private static class DefaultBuildOperationContext {
        private Throwable failure;
        private Object result;
        private String status;

        public void failed(Throwable t) {
            failure = t;
        }

        public void setResult(Object result) {
            this.result = result;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public void thrown(Throwable t) {
            if (failure == null) {
                failure = t;
            }
        }

        public Object getResult() {
            return result;
        }

        public String getStatus() {
            return status;
        }
    }
}
