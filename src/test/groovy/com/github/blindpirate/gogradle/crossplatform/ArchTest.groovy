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

class ArchTest {

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

    @Test(expected = IllegalArgumentException)
    void 'exception should be thrown when encountering unrecognized arch'() {
        Arch.of('unrecognized')
    }
}
