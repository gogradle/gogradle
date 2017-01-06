package com.github.blindpirate.gogradle.task

import com.github.blindpirate.gogradle.GogradleRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(GogradleRunner)
class BuildTaskTest extends TaskTest {
    BuildTask task

    @Before
    void setUp() {
        task = buildTask(BuildTask)
    }

    @Test
    void 'build task should be executed successfully'() {
        task.build()
    }
}
