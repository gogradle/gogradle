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
