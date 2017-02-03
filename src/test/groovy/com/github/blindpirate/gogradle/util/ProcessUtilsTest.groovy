package com.github.blindpirate.gogradle.util

import org.junit.Test

import static com.github.blindpirate.gogradle.util.ProcessUtils.ProcessResult
import static com.github.blindpirate.gogradle.util.ProcessUtils.runProcessWithCurrentClasspath

class ProcessUtilsTest {

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
        ProcessResult result = runProcessWithCurrentClasspath(Child, [], [:])
        // then
        assert result.code == 42
        assert result.stderr.contains('This is stderr')
        assert result.stdout.contains('This is stdout')
    }

}
