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

package com.github.blindpirate.gogradle.common

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.util.IOUtils
import org.junit.Test
import org.junit.runner.RunWith

import static com.github.blindpirate.gogradle.common.InSubpackagesPredicate.withRootDirAndSubpackages

/**
 * Determine if a file matches the specific subpackage set.
 *
 * Examples:<br>
 *
 * |--------file--------|--given subpackage-|-result-|<br>
 * |--------------------|-------------------|--------|<br>
 * |---------any--------|--------...--------|---√----|<br>
 * |-------file.go------|---------.---------|---√----|<br>
 * |-------file.go------|--------dir--------|---×----|<br>
 * |-----dir/file.go----|---------.---------|---×----|<br>
 * |-----dir/file.go----|--------dir--------|---√----|<br>
 * |-----dir/file.go----|-------dir/.-------|---√----|<br>
 * |-----dir/file.go----|----dir/subdir-----|---×----|<br>
 * |-dir/subdir/file.go-|----dir/subdir-----|---√----|<br>
 * |-dir/subdir/file.go-|--------dir--------|---√----|<br>
 */

@RunWith(GogradleRunner)
@WithResource('')
class InSubpackagesPredicateTest extends FileFilterTest {

    @Test
    void 'subpackages ... should accept everything'() {
        InSubpackagesPredicate predicate = withRootDirAndSubpackages(resource, ['...'] as Set)
        assert predicate.test(touch('a'))
        assert predicate.test(touch('.a'))
        assert predicate.test(touch('_a/.a'))
    }

    @Test(expected = IllegalStateException)
    void 'testing a directory should cause an exception'() {
        withRootDirAndSubpackages(resource, [] as Set).test(resource)
    }

    @Test
    void 'subpackage . should only accept file in rootDir'() {
        InSubpackagesPredicate predicate = withRootDirAndSubpackages(resource, ['.'] as Set)
        assert predicate.test(touch('file1'))
        assert !predicate.test(touch('dir1/file1'))
    }

    @Test
    void "sub/. should only accept sub's children"() {
        InSubpackagesPredicate predicate = withRootDirAndSubpackages(resource, ['dir/.'] as Set)
        assert predicate.test(touch('dir/file'))
        assert !predicate.test(touch('dir/dir/file'))
    }

    @Test
    void 'ordinary subpackage should take effect'() {
        InSubpackagesPredicate predicate = withRootDirAndSubpackages(resource, ['a', 'b', 'c/d'] as Set)
        assert predicate.test(touch('a/file'))
        assert predicate.test(touch('a/dir/file'))
        assert !predicate.test(touch('file'))

        assert predicate.test(touch('c/d'))
        IOUtils.forceDelete(new File(resource, 'c/d'))

        assert predicate.test(touch('c/d/file'))
        assert !predicate.test(touch('c/file'))
    }

}
