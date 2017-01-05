package com.github.blindpirate.gogradle.crossplatform

import org.junit.Test

class DefaultGoBinaryManagerTest {
    DefaultGoBinaryManager manager = new DefaultGoBinaryManager()

    @Test
    void 'smoke test'() {
        assert manager.binaryPath() == 'go'
    }

    @Test
    void 'local go binary should be returned if it exists and no version specified'() {

    }

    @Test
    void 'go binary with specified version should be downloaded'() {

    }


}
