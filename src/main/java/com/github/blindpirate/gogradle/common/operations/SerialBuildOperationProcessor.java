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

package com.github.blindpirate.gogradle.common.operations;

import org.gradle.api.Action;
import org.gradle.internal.operations.BuildOperation;
import org.gradle.internal.operations.BuildOperationProcessor;
import org.gradle.internal.operations.BuildOperationQueue;
import org.gradle.internal.operations.BuildOperationWorker;
import org.gradle.internal.operations.MultipleBuildOperationFailures;
import org.gradle.internal.operations.RunnableBuildOperation;

import javax.inject.Singleton;
import java.util.LinkedList;
import java.util.Queue;

@Singleton
public class SerialBuildOperationProcessor implements BuildOperationProcessor {
    @Override
    public <T extends BuildOperation> void run(BuildOperationWorker<T> worker,
                                               Action<BuildOperationQueue<T>> generator) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T extends RunnableBuildOperation> void run(Action<BuildOperationQueue<T>> generator) {
        SerialBuildOperationQueue queue = new SerialBuildOperationQueue();
        generator.execute(queue);
        queue.waitForCompletion();
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
            for (RunnableBuildOperation operation : queue) {
                operation.run();
            }
        }

        @Override
        public void setLogLocation(String logLocation) {
            throw new UnsupportedOperationException();
        }
    }
}
