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
import org.junit.Test
import org.junit.runner.RunWith

import static com.github.blindpirate.gogradle.util.ExceptionHandler.UncheckedException

@RunWith(GogradleRunner)
class CompressUtilsTest {

    File resource

    @Test(expected = IllegalArgumentException)
    @WithResource('')
    void 'exception should be thrown if file extension is not _tar_gz or _zip'() {
        IOUtils.write(resource, 'data.rar', '')
        CompressUtils.decompressZipOrTarGz(new File(resource, 'data.rar'), resource)
    }

    @Test(expected = UncheckedException)
    void 'exception should be thrown if error occurs in unzip process'() {
        CompressUtils.decompressZip(null, null)
    }

    @WithResource('')
    @Test(expected = UncheckedIOException)
    void 'exception should be thrown if IOException occurs in untar process'() {
        CompressUtils.decompressTarGz(new File(resource, 'unexistent'), resource)
    }
}
