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
