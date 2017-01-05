package com.github.blindpirate.gogradle.core

import org.junit.Test
import org.mockito.Mockito

class GolangTaskContainerTest {
    GolangTaskContainer taskContainer = new GolangTaskContainer()

    @Test
    void 'getting task instance after putting into container should success'() {
        GolangTaskContainer.TASKS.each { name, clazz ->
            def mock = Mockito.mock(clazz)
            taskContainer.put(clazz, mock)
            assert taskContainer.get(clazz).is(mock)
        }
    }
}
