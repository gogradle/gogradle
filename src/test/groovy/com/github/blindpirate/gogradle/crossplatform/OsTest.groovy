package com.github.blindpirate.gogradle.crossplatform

import com.github.blindpirate.gogradle.util.ReflectionUtils
import org.junit.Test

class OsTest {
    @Test
    void 'useless test'() {
        Os.values()
        Os.valueOf('WINDOWS')
    }

    @Test(expected = IllegalStateException)
    void 'exception should be thrown when auto-detect fails'() {
        ReflectionUtils.setStaticFinalField(Os.WINDOWS, 'OS_DETECTION_MAP', [:])
        ReflectionUtils.setStaticFinalField(Os.WINDOWS, 'hostOs', null)
        Os.getHostOs()
    }
}
