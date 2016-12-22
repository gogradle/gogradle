package com.github.blindpirate.gogradle.util

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.general.Factory
import com.google.common.base.Optional
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
    public void 'production with PickyFactory should success'() {
        // given:
        when(factory1.produce(material)).thenReturn(Optional.absent())
        when(factory2.produce(material)).thenReturn(Optional.of(product))

        // when:
        def result = FactoryUtil.produce([factory1, factory2], material)

        // then
        assert result.get() == product
    }

    @Test
    public void 'production when not accepted should fail'() {
        // given:
        when(factory1.produce(material)).thenReturn(Optional.absent())
        when(factory2.produce(material)).thenReturn(Optional.absent())

        // when:
        def result = FactoryUtil.produce([factory1, factory2], material)

        // then:
        assert !result.isPresent()
    }
}
