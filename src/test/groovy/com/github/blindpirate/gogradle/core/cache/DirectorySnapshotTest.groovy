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

package com.github.blindpirate.gogradle.core.cache

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.util.IOUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(GogradleRunner)
@WithResource('')
class DirectorySnapshotTest {
    File resource

    DirectorySnapshot snapshot

    @Before
    void setUp() {
        IOUtils.write(resource, 'a/b/c/1.go', '1')
        IOUtils.write(resource, 'a/b/c/2.go', '2')
        IOUtils.mkdir(resource, 'a/dir')
        snapshot = DirectorySnapshot.of(resource, new File(resource, 'a'))
    }

    @Test
    void 'it should be up-to-date if nothing changed'() {
        assert snapshot.isUpToDate(resource, new File(resource, 'a'))
    }

    @Test
    void 'it should be out-of-date when the entire dir is deleted'() {
        IOUtils.deleteQuitely(new File(resource, 'a'))
        assert !snapshot.isUpToDate(resource, new File(resource, 'a'))
    }

    @Test
    void 'it should be out-of-date when adding a file'() {
        IOUtils.write(resource, 'a/b/c/3.go', '3')
        assert !snapshot.isUpToDate(resource, new File(resource, 'a'))
    }

    @Test
    void 'it should be out-of-date when deleting a file'() {
        IOUtils.deleteQuitely(new File(resource, 'a/b/c/1.go'))
        assert !snapshot.isUpToDate(resource, new File(resource, 'a'))
    }

    @Test
    void 'it should be out-of-date when modified a file'() {
        File file = new File(resource, 'a/b/c/1.go')
        file.setLastModified(file.lastModified() - 2000)
        assert !snapshot.isUpToDate(resource, new File(resource, 'a'))
    }

    @Test
    void 'it should be out-of-date when adding a dir'() {
        IOUtils.mkdir(resource, 'a/b/c/d')
        assert !snapshot.isUpToDate(resource, new File(resource, 'a'))
    }

    @Test
    void 'it should be out-of-date when deleting a dir'() {
        IOUtils.deleteQuitely(new File(resource, 'a/dir'))
        assert !snapshot.isUpToDate(resource, new File(resource, 'a'))
    }

}
