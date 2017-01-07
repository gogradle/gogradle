package com.github.blindpirate.gogradle.crossplatform

import com.github.blindpirate.gogradle.util.ReflectionUtils
import org.junit.Test

class ArchTest {
    @Test
    void 'useless test'() {
        Arch.values()
        Arch.valueOf('I386')
    }

    @Test(expected = IllegalStateException)
    void 'exception should be thrown when auto-detected fails'() {
        ReflectionUtils.setStaticFinalField(Arch.I386, 'ARCH_DETECTION_MAP', [:])
        ReflectionUtils.setStaticFinalField(Arch.I386, 'hostArch', null)
        Arch.getHostArch()
    }
}
