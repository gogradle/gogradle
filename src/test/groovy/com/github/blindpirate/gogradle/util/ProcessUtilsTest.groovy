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

package com.github.blindpirate.gogradle.util

import org.junit.Test

class ProcessUtilsTest {

    ProcessUtils processUtils = new ProcessUtils()

    class Child {
        static void main(String[] args) {
            System.out.print('This is stdout')
            System.err.print('This is stderr')
            System.exit(42)
        }
    }

    @Test
    void 'getting result of child process should succeed'() {
        // when
        ProcessUtils.ProcessResult result = processUtils.runProcessWithCurrentClasspath(Child, [], [:])
        // then
        assert result.code == 42
        assert result.stderr.contains('This is stderr')
        assert result.stdout.contains('This is stdout')
    }

    @Test(expected = UncheckedIOException)
    void 'exception should be thrown if error occurs'() {
        processUtils.run('unexistent_program')
    }

}
