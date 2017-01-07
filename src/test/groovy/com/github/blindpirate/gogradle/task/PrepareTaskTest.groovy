package com.github.blindpirate.gogradle.task

import com.github.blindpirate.gogradle.GogradleRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(GogradleRunner)
class PrepareTaskTest extends TaskTest {

    PrepareTask task

    @Before
    void setUp() {
        task = buildTask(PrepareTask)
    }

    @Test
    void 'preparation should succeed'() {
        task.prepare()
    }

}
