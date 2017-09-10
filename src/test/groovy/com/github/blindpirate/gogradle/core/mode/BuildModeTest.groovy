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

package com.github.blindpirate.gogradle.core.mode

import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet
import com.github.blindpirate.gogradle.core.dependency.NotationDependency
import com.github.blindpirate.gogradle.util.DependencyUtils
import org.junit.Before
import org.junit.Test

import static com.github.blindpirate.gogradle.core.mode.BuildMode.*

class BuildModeTest {
    GolangDependencySet declared = GolangDependencySet.empty()
    GolangDependencySet locked = GolangDependencySet.empty()

    NotationDependency cInBuildDotGradle = DependencyUtils.mockWithName(NotationDependency, 'c')
    NotationDependency lockedC = DependencyUtils.mockWithName(NotationDependency, 'c')

    @Before
    void setUp() {
        declared.add(cInBuildDotGradle)
        locked.add(lockedC)
    }

    @Test
    void 'declared > locked in DEVELOP mode'() {
        // when
        GolangDependencySet result = BuildMode.DEVELOP.determine(declared, locked)
        // then
        assert result.any { it.is(cInBuildDotGradle) }
    }

    @Test
    void 'locked > declared in REPRODUCIBLE mode'() {
        // when
        GolangDependencySet result = BuildMode.REPRODUCIBLE.determine(declared, locked)
        // then
        assert result.any { it.is(lockedC) }
    }

    @Test
    void 'fromString should succeed'() {
        assert DEVELOP == fromString("DEVELOP")
        assert DEVELOP == fromString("DEV")
        assert REPRODUCIBLE == fromString("REPRODUCIBLE")
        assert REPRODUCIBLE == fromString("REP")
    }

    @Test(expected = IllegalArgumentException)
    void 'invalid build mode should throw exception'(){
        fromString('DEVE')
    }
}
