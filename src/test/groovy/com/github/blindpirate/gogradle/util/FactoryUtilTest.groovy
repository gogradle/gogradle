package com.github.blindpirate.gogradle.util

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.general.PickyFactory
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
class FactoryUtilTest {
    @Mock
    PickyFactory factory1
    @Mock
    PickyFactory factory2
    @Mock
    Object material
    @Mock
    Object product

    @Test
    public void 'production with PickyFactory should success'() {
        // given:
        when(factory1.accept(material)).thenReturn(false)
        when(factory2.accept(material)).thenReturn(true)
        when(factory2.produce(material)).thenReturn(product)

        // when:
        def result = FactoryUtil.produce([factory1, factory2], material)

        // then
        assert result.get() == product
    }

    @Test
    public void 'production when not accepted should fail'() {
        // given:
        when(factory1.accept(material)).thenReturn(false)
        when(factory2.accept(material)).thenReturn(false)

        // when:
        def result = FactoryUtil.produce([factory1, factory2], material)

        // then:
        assert !result.isPresent()
    }
}
