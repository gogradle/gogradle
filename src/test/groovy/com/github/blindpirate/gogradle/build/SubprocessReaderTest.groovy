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

package com.github.blindpirate.gogradle.build

import com.github.blindpirate.gogradle.GogradleRunner
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Mock

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.function.Consumer
import java.util.function.Supplier

import static org.mockito.ArgumentMatchers.any
import static org.mockito.ArgumentMatchers.anyInt
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
class SubprocessReaderTest {

    @Mock
    Consumer consumer
    @Mock
    Supplier supplier
    @Mock
    InputStream inputStream

    @Test
    void 'countdown latch should terminate when exception occurs'() {
        CountDownLatch latch = new CountDownLatch(1)
        // NPE will be thrown
        new SubprocessReader(null, null, latch).start()
        latch.await()
    }

    @Test
    void 'input stream should be consumed line by line'() {
        CountDownLatch latch = new CountDownLatch(1)
        new SubprocessReader(
                new Supplier<InputStream>() {
                    @Override
                    InputStream get() {
                        return new ByteArrayInputStream('1\n2'.getBytes('UTF8'))
                    }
                },
                consumer,
                latch).run()
        latch.await()
        verify(consumer).accept('1')
        verify(consumer).accept('2')
    }

    @Test
    void 'stacktrace of exception should be consumed'() {
        // given
        when(supplier.get()).thenReturn(inputStream)
        when(inputStream.read(any(byte[]), anyInt(), anyInt())).thenThrow(new IOException())
        // when
        CountDownLatch latch = new CountDownLatch(1)
        new SubprocessReader(supplier, consumer, latch).start()
        latch.await(1, TimeUnit.SECONDS)
        // then
        ArgumentCaptor captor = ArgumentCaptor.forClass(String)
        verify(consumer).accept(captor.capture())
        assert captor.getValue().contains('java.io.IOException')
    }
}
