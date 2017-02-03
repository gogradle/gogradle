package com.github.blindpirate.gogradle.build

import com.github.blindpirate.gogradle.GogradleRunner
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Mock

import java.util.concurrent.CountDownLatch
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
    void 'stacktrace of exception should be consumed'() {
        // given
        when(supplier.get()).thenReturn(inputStream)
        when(inputStream.read(any(byte[]), anyInt(), anyInt())).thenThrow(new IOException())
        // when
        new SubprocessReader(supplier, consumer, new CountDownLatch(1)).start()
        // then
        ArgumentCaptor captor = ArgumentCaptor.forClass(String)
        verify(consumer).accept(captor.capture())
        assert captor.getValue().contains('java.io.IOException')
    }
}
