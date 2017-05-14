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

    @Test
    void 'executable extension should be empty string except on Windows'() {
        values().each {
            if (it == WINDOWS) {
                assert it.exeExtension() == '.exe'
            } else {
                assert it.exeExtension() == ''
            }
        }
    }

    @Test
    void 'archive extension should be .tar.gz except on Windows'() {
        values().each {
            if (it == WINDOWS) {
                assert it.archiveExtension() == '.zip'
            } else {
                assert it.archiveExtension() == '.tar.gz'
            }
        }
    }
}
