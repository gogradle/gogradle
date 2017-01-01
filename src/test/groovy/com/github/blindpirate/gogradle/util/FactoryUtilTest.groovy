package com.github.blindpirate.gogradle.util

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.general.Factory
import java.util.Optional
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
class FactoryUtilTest {
    @Mock
    Factory factory1
    @Mock
    Factory factory2
    @Mock
    Object material
    @Mock
    Object product

    @Test
    void 'production with PickyFactory should success'() {
        // given:
        when(factory1.produce(material)).thenReturn(Optional.empty())
        when(factory2.produce(material)).thenReturn(Optional.of(product))

        // when:
        def result = FactoryUtil.produce([factory1, factory2], material)

        // then
        assert result.get() == product
    }

    @Test
    void 'production when not accepted should fail'() {
        // given:
        when(factory1.produce(material)).thenReturn(Optional.empty())
        when(factory2.produce(material)).thenReturn(Optional.empty())

        // when:
        def result = FactoryUtil.produce([factory1, factory2], material)

        // then:
        assert !result.isPresent()
    }
}
