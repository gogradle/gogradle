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
    void 'exception should be thrown when auto-detect fails'() {
        synchronized (Arch) {
            String arch = System.getProperty('os.arch')
            try {
                System.setProperty('os.arch', 'invalid arch')
                ReflectionUtils.setStaticFinalField(Arch, 'hostArch', null)
                Arch.getHostArch()
            } finally {
                System.setProperty('os.arch', arch)
            }
        }
    }
}
