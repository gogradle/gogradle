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

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.util.IOUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

import java.util.function.Predicate

import static com.github.blindpirate.gogradle.util.IOUtils.*

@RunWith(GogradleRunner)
@WithResource('')
class MarkAndDeleteDirectoryVisitorTest {
    File resource

    /*

     resource
        |---- a (ancestor of marked)
        |     |-- c
        |     \-- d (ancestor of marked)
        |         \-- f (marked)
        |             \-- g
        \-- b
            \-- e
     */

    @Before
    void setUp() {
        mkdir(resource, 'a/c')
        mkdir(resource, 'a/d/f/g')
        mkdir(resource, 'b/e')
    }

    @Test
    void 'marking should succeed'() {
        MarkDirectoryVisitor visitor = mark()
        assert visitor.markedDirectories == dirSet('a/d/f')
        assert visitor.ancestorsOfMarkedDirectories == dirSet('a', 'a/d', '')
    }

    private MarkDirectoryVisitor mark() {
        MarkDirectoryVisitor visitor = new MarkDirectoryVisitor(resource, new Predicate<File>() {
            @Override
            boolean test(File file) {
                return 'f' == file.name
            }
        })

        walkFileTreeSafely(resource.toPath(), visitor)
        return visitor
    }


    @Test
    void 'deleting marked dir should succeed'() {
        DeleteUnmarkedDirectoryVisitor visitor = new DeleteUnmarkedDirectoryVisitor(mark())
        walkFileTreeSafely(resource.toPath(), visitor)
        assert new File(resource, 'a/d/f/g').exists()
        assert !new File(resource, 'a/c').exists()
        assert !new File(resource, 'b/e').exists()
    }

    Set dirSet(String... dirs) {
        return dirs.collect { new File(resource, it) } as Set
    }
}
