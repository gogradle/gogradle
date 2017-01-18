package com.github.blindpirate.gogradle.crossplatform

import com.github.blindpirate.gogradle.util.ReflectionUtils
import org.junit.Test

import static com.github.blindpirate.gogradle.crossplatform.Os.*
import static org.apache.commons.lang3.SystemUtils.*

class OsTest {
    @Test(expected = IllegalStateException)
    void 'exception should be thrown when auto-detect fails'() {
        synchronized (Os) {
            try {
                ReflectionUtils.setStaticFinalField(Os, 'OS_DETECTION_MAP', [:])
                ReflectionUtils.setStaticFinalField(Os, 'hostOs', null)
                getHostOs()
            } finally {
                ReflectionUtils.setStaticFinalField(Os, 'OS_DETECTION_MAP',
                        [(LINUX)  : IS_OS_LINUX,
                         (WINDOWS): IS_OS_WINDOWS,
                         (DARWIN) : IS_OS_MAC_OSX,
                         (FREEBSD): IS_OS_FREE_BSD,
                         (NETBSD) : IS_OS_NET_BSD,
                         (SOLARIS): IS_OS_SOLARIS,
                         (ZOS)    : IS_OS_ZOS])
            }
        }
    }
}
