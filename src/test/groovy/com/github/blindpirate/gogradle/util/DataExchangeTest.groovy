package com.github.blindpirate.gogradle.util

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings
import org.junit.Test

class DataExchangeTest {
    @Test(expected = IllegalStateException)
    void 'unchecked exception should be thrown if IOException occurs'() {
        DataExchange.toYaml(new Object() {
            @SuppressFBWarnings('UMAC_UNCALLABLE_METHOD_OF_ANONYMOUS_CLASS')
            int getId() {
                throw new IOException()
            }
        })
    }
}
