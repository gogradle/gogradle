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
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.util.IOUtils
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(GogradleRunner)
@WithResource('')
class DependencyInstallFileFilterTest {
    File resource

    File touch(String name) {
        IOUtils.write(resource, name, '')
        return new File(resource, name)
    }

    File mkdir(String dirName) {
        IOUtils.mkdir(resource, dirName)
        return new File(resource, dirName)
    }

    void allNamesAccepted(String... names) {
        names.every {
            assert DependencyInstallFileFilter.INSTANCE.accept(touch(it));
        }
    }

    void allDirNamesRejected(String... dirNames) {
        dirNames.each {
            assert !DependencyInstallFileFilter.INSTANCE.accept(mkdir(it))
        }
    }

    void allNamesRejected(String... names) {
        names.each {
            assert !DependencyInstallFileFilter.INSTANCE.accept(touch(it))
        }
    }

    @Test
    void '*_test.go should be rejected'() {
        allNamesRejected('_test.go', 'whatever_test.go')
    }

    @Test
    void 'file with .go/.asm/.s extensioin can be accepted'() {
        allNamesAccepted('1.go', 'main.go', '1.s', '1.asm', '1.h', '1.c')
        allNamesRejected('1', 'main', '1.jpg', 'main.java')
    }

    @Test
    void 'file starting with _ or . should be rejected'() {
        allNamesRejected('_.go', '..go', '_main.go')
    }

    @Test
    void 'directory named vendor or testdata should be rejected'() {
        allDirNamesRejected('vendor', 'testdata')
    }

    @Test
    void 'directory starting wtih _ or . should be rejected'() {
        allDirNamesRejected('_dir', '.dir')
    }

    @Test
    void 'directory should be rejected if all children are rejected'() {
        IOUtils.write(resource, '_dir/main.go', '')
        IOUtils.write(resource, '_main.go', '')

        assert !DependencyInstallFileFilter.INSTANCE.accept(resource)
    }

    @Test
    void 'directory should be accepted if any child is accepted'() {
        IOUtils.write(resource, '_dir/main.go', '')
        IOUtils.write(resource, 'main.go', '')

        assert DependencyInstallFileFilter.INSTANCE.accept(resource)
    }
}
