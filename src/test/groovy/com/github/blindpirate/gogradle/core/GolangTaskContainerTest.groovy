package com.github.blindpirate.gogradle.core

import com.github.blindpirate.gogradle.task.GolangTaskContainer
import org.junit.Test
import org.mockito.Mockito

class GolangTaskContainerTest {
    GolangTaskContainer taskContainer = new GolangTaskContainer()

    @Test
    void 'getting task instance after putting into container should succeed'() {
        GolangTaskContainer.TASKS.each { name, clazz ->
            def mock = Mockito.mock(clazz)
            taskContainer.put(clazz, mock)
            assert taskContainer.get(clazz).is(mock)
        }
    }
}
