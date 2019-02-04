package com.github.blindpirate.gogradle.task.go.test

import com.github.blindpirate.gogradle.util.DataExchange
import org.junit.Test

import static org.gradle.api.tasks.testing.TestResult.ResultType
import static org.gradle.api.tasks.testing.TestResult.ResultType.FAILURE
import static org.gradle.api.tasks.testing.TestResult.ResultType.SUCCESS

class GoTestEventTest {
    //    {"Time":"2018-10-12T19:57:48.727973+08:00","Action":"run","Package":"a","Test":"Test_A1_1"}
    //    {"Time":"2018-10-12T19:57:48.728171+08:00","Action":"output","Package":"a","Test":"Test_A1_1","Output":"=== RUN   Test_A1_1\n"}
    //    {"Time":"2018-10-12T19:57:48.728187+08:00","Action":"output","Package":"a","Test":"Test_A1_1","Output":"--- FAIL: Test_A1_1 (0.00s)\n"}
    //    {"Time":"2018-10-12T19:57:48.728193+08:00","Action":"output","Package":"a","Test":"Test_A1_1","Output":"    a1_test.go:9: Failed\n"}
    //    {"Time":"2018-10-12T19:57:48.728209+08:00","Action":"fail","Package":"a","Test":"Test_A1_1","Elapsed":0}
    @Test
    void 'can convert test events'() {
        assertEvent('{"Time":"2019-01-27T17:57:12.021037965+01:00","Action":"run","Package":"github.com/apache/beam/sdks/go/pkg/beam/core/metrics","Test":"TestCounter_Inc"}',
                'TestCounter_Inc', null, null, null)
        assertEvent('{"Time":"2019-01-27T17:57:12.021301568+01:00","Action":"output","Package":"github.com/apache/beam/sdks/go/pkg/beam/core/metrics","Test":"TestCounter_Inc","Output":"=== RUN   TestCounter_Inc\\n"}',
                'TestCounter_Inc', "=== RUN   TestCounter_Inc\n", null, null)
        assertNullEvent('{"Time":"2019-01-27T17:57:12.02131797+01:00","Action":"run   testcounter_inc/add_1_to_inc1.count[\\"a\\"]_value","Package":"github.com/apache/beam/sdks/go/pkg/beam/core/metrics","Test":"_1"}')
        assertEvent('{"Time":"2019-01-27T17:57:12.021332794+01:00","Action":"output","Package":"github.com/apache/beam/sdks/go/pkg/beam/core/metrics","Test":"_1","Output":"=== RUN   TestCounter_Inc/add_1_to_inc1.count[\\"A\\"]_value:_1\\n"}',
                'TestCounter_Inc/add_1_to_inc1.count["A"]_value:_1', '=== RUN   TestCounter_Inc/add_1_to_inc1.count["A"]_value:_1\n', null, null)
        assertEvent('{"Time":"2019-01-27T17:57:12.021506429+01:00","Action":"output","Package":"github.com/apache/beam/sdks/go/pkg/beam/core/metrics","Test":"TestCounter_Inc/add_1_to_inc1.count[\\"A\\"]_value:_1","Output":"    --- PASS: TestCounter_Inc/add_1_to_inc1.count[\\"A\\"]_value:_1 (0.00s)\\n"}',
                'TestCounter_Inc/add_1_to_inc1.count["A"]_value:_1', '    --- PASS: TestCounter_Inc/add_1_to_inc1.count["A"]_value:_1 (0.00s)\n', SUCCESS, 0L)
        assertEvent('{"Time":"2019-01-27T17:57:12.021516845+01:00","Action":"pass","Package":"github.com/apache/beam/sdks/go/pkg/beam/core/metrics","Test":"TestCounter_Inc/add_1_to_inc1.count[\\"A\\"]_value:_1","Elapsed":0}',
                'TestCounter_Inc/add_1_to_inc1.count["A"]_value:_1', null, SUCCESS, 0L)
        assertEvent('{"Time":"2018-10-12T19:57:48.728171+08:00","Action":"output","Package":"a","Test":"Test_A1_1","Output":"=== RUN   Test_A1_1\\n"}',
                'Test_A1_1', '=== RUN   Test_A1_1\n', null, null)
        assertEvent('{"Time":"2018-10-12T19:57:48.728187+08:00","Action":"output","Package":"a","Test":"Test_A1_1","Output":"--- FAIL: Test_A1_1 (0.00s)\\n"}',
                'Test_A1_1', '--- FAIL: Test_A1_1 (0.00s)\n', FAILURE, 0L)
        assertEvent('{"Time":"2018-10-12T19:57:48.728193+08:00","Action":"output","Package":"a","Test":"Test_A1_1","Output":"    a1_test.go:9: Failed\\n"}',
                'Test_A1_1', '    a1_test.go:9: Failed\n', null, null)
        assertEvent('{"Time":"2018-10-12T19:57:48.728209+08:00","Action":"fail","Package":"a","Test":"Test_A1_1","Elapsed":0}',
                'Test_A1_1', null, FAILURE, 0L)
    }

    private void assertNullEvent(String json) {
        assert DataExchange.parseJson(json, GoTestResultJsonModel).toTestEvent() == null
    }

    private void assertEvent(String json, String testName, String output, ResultType resultType, Long ms) {
        GoTestEvent event = DataExchange.parseJson(json, GoTestResultJsonModel).toTestEvent()
        assert event.testName == testName
        assert event.output == output
        assert event.resultType == resultType
        assert event.durationMillis == ms
    }
}