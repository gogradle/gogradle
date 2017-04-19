package com.github.blindpirate.gogradle.core.cache

import com.github.blindpirate.gogradle.core.GolangCloneable
import com.github.blindpirate.gogradle.util.ReflectionUtils
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings
import groovy.transform.EqualsAndHashCode
import org.apache.commons.collections4.map.LRUMap
import org.junit.Before
import org.junit.Test

import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Function

import static org.mockito.Mockito.CALLS_REAL_METHODS
import static org.mockito.Mockito.mock

class AbstractCacheTest {

    AbstractCache cache = mock(AbstractCache, CALLS_REAL_METHODS)

    @Before
    void setUp() {
        ReflectionUtils.setField(cache, 'container', new LRUMap())
    }

    @Test
    void 'constructor should be called when cached item does not exist'() {
        AtomicInteger counter = new AtomicInteger(0)
        def result = cache.get(42, new Function<Integer, GolangCloneableForTest>() {
            @Override
            GolangCloneableForTest apply(Integer i) {
                counter.incrementAndGet()
                return new GolangCloneableForTest(value: i)
            }
        })

        assert result.value == 42
        assert counter.get() == 1
    }

    @Test
    void 'result should be cloned when returned'() {
        'constructor should be called when cached item does not exist'()

        def result1 = cache.get(42, null)
        def result2 = cache.get(42, null)
        assert result1.value == 42
        assert result2.value == 42
        assert !result1.is(result2)
    }

    @EqualsAndHashCode
    static class GolangCloneableForTest implements GolangCloneable, Serializable {
        private static final long serialVersionUID = 1
        int value

        @SuppressFBWarnings("CN_IDIOM_NO_SUPER_CALL")
        Object clone() {
            return super.clone()
        }
    }
}
