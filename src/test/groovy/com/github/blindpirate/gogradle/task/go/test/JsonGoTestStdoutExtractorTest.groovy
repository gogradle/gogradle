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

package com.github.blindpirate.gogradle.task.go.test

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.task.go.PackageTestResult
import com.github.blindpirate.gogradle.util.IOUtils
import org.gradle.api.internal.tasks.testing.junit.result.TestClassResult
import org.gradle.api.tasks.testing.TestResult
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(GogradleRunner)
@WithResource('')
class JsonGoTestStdoutExtractorTest {

    JsonGoTestResultExtractor extractor = new JsonGoTestResultExtractor()

    File resource

    @Test
    void 'extracting one result should succeed'() {
        // given
        IOUtils.write(resource, 'a1_test.go', 'func Test_A1_1(){}  func Test_A1_2(){}')
        IOUtils.write(resource, 'a2_test.go', 'func Test_A2_1()')
        String stdout = '''\
{"Time":"2018-10-13T07:45:56.436361+08:00","Action":"run","Package":"a","Test":"Test_A1_1"}
{"Time":"2018-10-13T07:45:56.437208+08:00","Action":"output","Package":"a","Test":"Test_A1_1","Output":"=== RUN   Test_A1_1\\n"}
{"Time":"2018-10-13T07:45:56.437232+08:00","Action":"output","Package":"a","Test":"Test_A1_1","Output":"--- FAIL: Test_A1_1 (0.00s)\\n"}
{"Time":"2018-10-13T07:45:56.437239+08:00","Action":"output","Package":"a","Test":"Test_A1_1","Output":"    a1_test.go:9: Failed\\n"}
{"Time":"2018-10-13T07:45:56.437244+08:00","Action":"fail","Package":"a","Test":"Test_A1_1","Elapsed":0}
{"Time":"2018-10-13T07:45:56.437256+08:00","Action":"run","Package":"a","Test":"Test_A1_2"}
{"Time":"2018-10-13T07:45:56.43726+08:00","Action":"output","Package":"a","Test":"Test_A1_2","Output":"=== RUN   Test_A1_2\\n"}
{"Time":"2018-10-13T07:45:56.437265+08:00","Action":"output","Package":"a","Test":"Test_A1_2","Output":"--- PASS: Test_A1_2 (0.00s)\\n"}
{"Time":"2018-10-13T07:45:56.437269+08:00","Action":"output","Package":"a","Test":"Test_A1_2","Output":"    a1_test.go:15: Passed\\n"}
{"Time":"2018-10-13T07:45:56.437364+08:00","Action":"pass","Package":"a","Test":"Test_A1_2","Elapsed":0}
{"Time":"2018-10-13T07:45:56.437377+08:00","Action":"run","Package":"a","Test":"Test_A2_1"}
{"Time":"2018-10-13T07:45:56.437382+08:00","Action":"output","Package":"a","Test":"Test_A2_1","Output":"=== RUN   Test_A2_1\\n"}
{"Time":"2018-10-13T07:45:56.437388+08:00","Action":"output","Package":"a","Test":"Test_A2_1","Output":"--- PASS: Test_A2_1 (0.00s)\\n"}
{"Time":"2018-10-13T07:45:56.437392+08:00","Action":"output","Package":"a","Test":"Test_A2_1","Output":"    a2_test.go:7: Passed\\n"}
{"Time":"2018-10-13T07:45:56.437401+08:00","Action":"pass","Package":"a","Test":"Test_A2_1","Elapsed":0}
{"Time":"2018-10-13T07:45:56.437405+08:00","Action":"output","Package":"a","Output":"FAIL\\n"}
{"Time":"2018-10-13T07:45:56.437457+08:00","Action":"output","Package":"a","Output":"FAIL\\ta\\t0.006s\\n"}
{"Time":"2018-10-13T07:45:56.437467+08:00","Action":"fail","Package":"a","Elapsed":0.006}
'''
        // when
        PackageTestResult context = PackageTestResult.builder()
                .withPackagePath('a/b/c')
                .withStdout(stdout.split(/\n/) as List)
                .withTestFiles(resource.listFiles() as List)
                .build()
        List<TestClassResult> results = extractor.extractTestResult(context)

        assert results.size() == 2

        TestClassResult result0 = results[0]
        assert result0.className == 'a.b.c.a1_test_DOT_go'
        assert result0.results.size() == 2

        assert result0.results[0].name == 'Test_A1_1'
        assert result0.results[0].message.trim().contains('Test_A1_1')
        assert result0.results[0].resultType == TestResult.ResultType.FAILURE

        assert result0.results[1].name == 'Test_A1_2'
        assert result0.results[1].message.trim().contains('Test_A1_2')
        assert result0.results[1].resultType == TestResult.ResultType.SUCCESS

        TestClassResult result1 = results[1]
        assert result1.className == 'a.b.c.a2_test_DOT_go'
        assert result1.results.size() == 1

        assert result1.results[0].name == 'Test_A2_1'
        assert result1.results[0].message.trim().contains('Test_A2_1')
        assert result1.results[0].resultType == TestResult.ResultType.SUCCESS
    }
}
