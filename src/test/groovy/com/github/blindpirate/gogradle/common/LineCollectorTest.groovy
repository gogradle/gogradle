package com.github.blindpirate.gogradle.common

import org.junit.Test

class LineCollectorTest {
    @Test
    void 'collecting lines should succeed'() {
        LineCollector collector = new LineCollector();
        collector.accept("1")
        collector.accept("2")

        assert collector.getOutput() == '1\n2'
    }
}
