package com.github.blindpirate.gogradle.common.operations

import org.gradle.api.Action
import org.gradle.internal.operations.BuildOperationQueue
import org.gradle.internal.operations.RunnableBuildOperation
import org.junit.Test

class SerialBuildOperationProcessorTest {

    SerialBuildOperationProcessor processor = new SerialBuildOperationProcessor()

    int counter = 0

    @Test
    void 'all operation should be executed sequentially'() {
        // when
        processor.run(new Action<BuildOperationQueue>() {
            @Override
            void execute(BuildOperationQueue buildOperationQueue) {
                1000.times {
                    buildOperationQueue.add(new Incrementer())
                }
            }
        })
        // then
        assert counter == 1000
    }

    @Test(expected = UnsupportedOperationException)
    void 'other method is not supported'() {
        processor.run(null, null)
    }

    @Test(expected = UnsupportedOperationException)
    void 'cancel() is not supported'() {
        processor.run(new Action<BuildOperationQueue>() {
            @Override
            void execute(BuildOperationQueue buildOperationQueue) {
                buildOperationQueue.cancel()
            }
        })
    }

    @Test(expected = UnsupportedOperationException)
    void 'setLogLocation() is not supported'() {
        processor.run(new Action<BuildOperationQueue>() {
            @Override
            void execute(BuildOperationQueue buildOperationQueue) {
                buildOperationQueue.setLogLocation()
            }
        })
    }

    class Incrementer implements RunnableBuildOperation {
        @Override
        String getDescription() {
            return null
        }

        @Override
        void run() {
            counter++
        }
    }
}
