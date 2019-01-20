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

package com.github.blindpirate.gogradle.core.dependency.install

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.common.FileFilterTest
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.util.IOUtils
import org.junit.Test
import org.junit.runner.RunWith

import java.nio.file.Files
import java.nio.file.Paths

@RunWith(GogradleRunner)
@WithResource('')
class DependencyInstallFileFilterTest extends FileFilterTest {
    DependencyInstallFileFilter allDescendentFilter = DependencyInstallFileFilter.subpackagesFilter(resource, ['...'] as Set)

    void allNamesAccepted(DependencyInstallFileFilter filter, String... names) {
        names.every {
            assert filter.accept(touch(it))
        }
    }

    void allDirNamesRejected(DependencyInstallFileFilter filter, String... dirNames) {
        dirNames.each {
            assert !filter.accept(mkdir(it))
        }
    }

    void allNamesRejected(DependencyInstallFileFilter filter, String... names) {
        names.each {
            assert !filter.accept(touch(it))
        }
    }

    @Test
    void '*_test_go should be rejected'() {
        allNamesRejected(allDescendentFilter, '_test.go', 'whatever_test.go')
    }

    @Test
    void 'file with _go _asm _s extension can be accepted'() {
        allNamesAccepted(allDescendentFilter, '1.go', 'main.go', '1.s', '1.asm', '1.h', '1.c')
        allNamesRejected(allDescendentFilter, '1', 'main', '1.jpg', 'main.java')
    }

    @Test
    void 'file containing _so should be accepted'() {
        allNamesAccepted(allDescendentFilter, "1.so", '1.so.1', 'libxxx.so.1.2')
    }

    @Test
    void 'file starting with _ or dot should be rejected'() {
        allNamesRejected(allDescendentFilter, '_.go', '..go', '_main.go')
    }

    @Test
    void 'directory named vendor or testdata should be rejected'() {
        allDirNamesRejected(allDescendentFilter, 'vendor', 'testdata')
    }

    @Test
    void 'directory starting wtih _ or dot should be rejected'() {
        allDirNamesRejected(allDescendentFilter, '_dir', '.dir')
    }

    @Test
    void 'directory should be rejected if all children are rejected'() {
        IOUtils.write(resource, '_dir/main.go', '')
        IOUtils.write(resource, '_main.go', '')

        assert !allDescendentFilter.accept(resource)
    }

    @Test
    void 'relative symlink should be rejected'() {
        Files.createSymbolicLink(resource.toPath().resolve('link'), Paths.get(".."))

        assert !allDescendentFilter.accept(new File(resource, 'link'))
    }

    @Test
    void 'absolute symlink should be rejected'() {
        Files.createSymbolicLink(resource.toPath().resolve('link'), resource.toPath())

        assert !allDescendentFilter.accept(new File(resource, 'link'))
    }

    @Test
    void 'directory should be accepted if any child is accepted'() {
        IOUtils.write(resource, '_dir/main.go', '')
        IOUtils.write(resource, 'main.go', '')

        assert allDescendentFilter.accept(resource)
    }

    @Test
    void 'inSubpackage filter should take effect'() {
        def filter = DependencyInstallFileFilter.subpackagesFilter(resource, ['.'] as Set)
        allNamesAccepted(filter, 'a.go')
        allNamesRejected(filter, 'a/a.go')
        allDirNamesRejected(filter, 'a')
    }
}
