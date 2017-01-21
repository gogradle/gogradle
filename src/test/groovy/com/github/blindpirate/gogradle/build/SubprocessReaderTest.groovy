package com.github.blindpirate.gogradle.build

import org.junit.Test

import java.util.concurrent.CountDownLatch

class SubprocessReaderTest {
    @Test
    void 'countdown latch should terminate when exception occurs'() {
        CountDownLatch latch = new CountDownLatch(1)
        // NPE will be thrown
        new SubprocessReader(null, null, latch).start()
        latch.await()
    }
}
