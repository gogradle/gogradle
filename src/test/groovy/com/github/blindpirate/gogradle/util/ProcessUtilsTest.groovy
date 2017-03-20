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

}
