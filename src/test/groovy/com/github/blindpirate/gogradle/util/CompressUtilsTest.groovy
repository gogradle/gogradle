package com.github.blindpirate.gogradle.util

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.support.WithResource
import org.junit.Test
import org.junit.runner.RunWith

import static com.github.blindpirate.gogradle.util.ExceptionHandler.*

@RunWith(GogradleRunner)
class CompressUtilsTest {

    File resource

    @Test(expected = IllegalArgumentException)
    @WithResource('')
    void 'exception should be thrown if file extension is not .tar.gz or .zip'() {
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
