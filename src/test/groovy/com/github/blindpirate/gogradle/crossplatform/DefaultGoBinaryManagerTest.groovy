package com.github.blindpirate.gogradle.crossplatform

import org.junit.Test

class DefaultGoBinaryManagerTest {
    DefaultGoBinaryManager manager = new DefaultGoBinaryManager()

    @Test
    void 'smoke test'() {
        assert manager.binaryPath() == 'go'
    }
}
