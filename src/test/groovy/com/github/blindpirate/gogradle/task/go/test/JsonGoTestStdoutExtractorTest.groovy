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

    // https://github.com/gogradle/gogradle/issues/276
    @Test
    void 'can extract parameterized test result'() {
        // given
        IOUtils.write(resource, "datastore_test.go", '''
func Test_keyLessThan(t *testing.T) {
    tsts := []struct {
        a      *datastore.Key
        b      *datastore.Key
        expect bool
        name   string
    }{
        {
            name:   "a<b",
            a:      datastore.NameKey("A", "a", nil),
            b:      datastore.NameKey("A", "b", nil),
            expect: true,
        },
        {
            name:   "b>a",
            a:      datastore.NameKey("A", "b", nil),
            b:      datastore.NameKey("A", "a", nil),
            expect: false,
        },
        {
            name:   "a=a",
            a:      datastore.NameKey("A", "a", nil),
            b:      datastore.NameKey("A", "a", nil),
            expect: false,
        },
        {
            name:   "a.a<b",
            a:      datastore.NameKey("A", "a", nil),
            b:      datastore.NameKey("A", "a", datastore.NameKey("A", "b", nil)),
            expect: true,
        },
        {
            name:   "a.a<a.b",
            a:      datastore.NameKey("A", "a", datastore.NameKey("A", "a", nil)),
            b:      datastore.NameKey("A", "a", datastore.NameKey("A", "b", nil)),
            expect: true,
        },
        {
            name:   "a.b>a.a",
            a:      datastore.NameKey("A", "a", datastore.NameKey("A", "b", nil)),
            b:      datastore.NameKey("A", "a", datastore.NameKey("A", "a", nil)),
            expect: false,
        },
        {
            name:   "a.a=a.a",
            a:      datastore.NameKey("A", "a", datastore.NameKey("A", "a", nil)),
            b:      datastore.NameKey("A", "a", datastore.NameKey("A", "a", nil)),
            expect: false,
        },
        {
            name:   "4dda<A",
            a:      datastore.NameKey("A", "4dda", nil),
            b:      datastore.NameKey("A", "A", nil),
            expect: true,
        },
    }
    for n := range tsts {
        index := n
        t.Run(tsts[index].name, func(t *testing.T) {
            got := keyLessThan(tsts[index].a, tsts[index].b)
            if tsts[index].expect != got {
                t.Fail()
            }
        })
    }
}
''')
        String stdout = '''\
{"Time":"2019-01-23T17:06:20.437250944+01:00","Action":"output","Package":"github.com/apache/beam/sdks/go/pkg/beam/io/datastoreio","Output":"2019/01/23 17:06:20 Func for func(reflect.Type, []uint8) (typex.T, error) already registered. Overwriting.\\n"}
{"Time":"2019-01-23T17:06:20.437615238+01:00","Action":"run","Package":"github.com/apache/beam/sdks/go/pkg/beam/io/datastoreio","Test":"Test_keyLessThan"}
{"Time":"2019-01-23T17:06:20.437622508+01:00","Action":"output","Package":"github.com/apache/beam/sdks/go/pkg/beam/io/datastoreio","Test":"Test_keyLessThan","Output":"=== RUN   Test_keyLessThan\\n"}
{"Time":"2019-01-23T17:06:20.437630822+01:00","Action":"run","Package":"github.com/apache/beam/sdks/go/pkg/beam/io/datastoreio","Test":"Test_keyLessThan/a\\u003cb"}
{"Time":"2019-01-23T17:06:20.437638036+01:00","Action":"output","Package":"github.com/apache/beam/sdks/go/pkg/beam/io/datastoreio","Test":"Test_keyLessThan/a\\u003cb","Output":"=== RUN   Test_keyLessThan/a\\u003cb\\n"}
{"Time":"2019-01-23T17:06:20.437643205+01:00","Action":"run","Package":"github.com/apache/beam/sdks/go/pkg/beam/io/datastoreio","Test":"Test_keyLessThan/b\\u003ea"}
{"Time":"2019-01-23T17:06:20.437649224+01:00","Action":"output","Package":"github.com/apache/beam/sdks/go/pkg/beam/io/datastoreio","Test":"Test_keyLessThan/b\\u003ea","Output":"=== RUN   Test_keyLessThan/b\\u003ea\\n"}
{"Time":"2019-01-23T17:06:20.437653965+01:00","Action":"run","Package":"github.com/apache/beam/sdks/go/pkg/beam/io/datastoreio","Test":"Test_keyLessThan/a=a"}
{"Time":"2019-01-23T17:06:20.43765827+01:00","Action":"output","Package":"github.com/apache/beam/sdks/go/pkg/beam/io/datastoreio","Test":"Test_keyLessThan/a=a","Output":"=== RUN   Test_keyLessThan/a=a\\n"}
{"Time":"2019-01-23T17:06:20.437663358+01:00","Action":"run","Package":"github.com/apache/beam/sdks/go/pkg/beam/io/datastoreio","Test":"Test_keyLessThan/a.a\\u003cb"}
{"Time":"2019-01-23T17:06:20.437667819+01:00","Action":"output","Package":"github.com/apache/beam/sdks/go/pkg/beam/io/datastoreio","Test":"Test_keyLessThan/a.a\\u003cb","Output":"=== RUN   Test_keyLessThan/a.a\\u003cb\\n"}
{"Time":"2019-01-23T17:06:20.437672455+01:00","Action":"run","Package":"github.com/apache/beam/sdks/go/pkg/beam/io/datastoreio","Test":"Test_keyLessThan/a.a\\u003ca.b"}
{"Time":"2019-01-23T17:06:20.437676701+01:00","Action":"output","Package":"github.com/apache/beam/sdks/go/pkg/beam/io/datastoreio","Test":"Test_keyLessThan/a.a\\u003ca.b","Output":"=== RUN   Test_keyLessThan/a.a\\u003ca.b\\n"}
{"Time":"2019-01-23T17:06:20.43768485+01:00","Action":"run","Package":"github.com/apache/beam/sdks/go/pkg/beam/io/datastoreio","Test":"Test_keyLessThan/a.b\\u003ea.a"}
{"Time":"2019-01-23T17:06:20.437693009+01:00","Action":"output","Package":"github.com/apache/beam/sdks/go/pkg/beam/io/datastoreio","Test":"Test_keyLessThan/a.b\\u003ea.a","Output":"=== RUN   Test_keyLessThan/a.b\\u003ea.a\\n"}
{"Time":"2019-01-23T17:06:20.437701737+01:00","Action":"run","Package":"github.com/apache/beam/sdks/go/pkg/beam/io/datastoreio","Test":"Test_keyLessThan/a.a=a.a"}
{"Time":"2019-01-23T17:06:20.437708012+01:00","Action":"output","Package":"github.com/apache/beam/sdks/go/pkg/beam/io/datastoreio","Test":"Test_keyLessThan/a.a=a.a","Output":"=== RUN   Test_keyLessThan/a.a=a.a\\n"}
{"Time":"2019-01-23T17:06:20.437713257+01:00","Action":"run","Package":"github.com/apache/beam/sdks/go/pkg/beam/io/datastoreio","Test":"Test_keyLessThan/4dda\\u003cA"}
{"Time":"2019-01-23T17:06:20.437717637+01:00","Action":"output","Package":"github.com/apache/beam/sdks/go/pkg/beam/io/datastoreio","Test":"Test_keyLessThan/4dda\\u003cA","Output":"=== RUN   Test_keyLessThan/4dda\\u003cA\\n"}
{"Time":"2019-01-23T17:06:20.437726371+01:00","Action":"output","Package":"github.com/apache/beam/sdks/go/pkg/beam/io/datastoreio","Test":"Test_keyLessThan","Output":"--- PASS: Test_keyLessThan (0.00s)\\n"}
{"Time":"2019-01-23T17:06:20.437735311+01:00","Action":"output","Package":"github.com/apache/beam/sdks/go/pkg/beam/io/datastoreio","Test":"Test_keyLessThan/a\\u003cb","Output":"    --- PASS: Test_keyLessThan/a\\u003cb (0.00s)\\n"}
{"Time":"2019-01-23T17:06:20.437742561+01:00","Action":"pass","Package":"github.com/apache/beam/sdks/go/pkg/beam/io/datastoreio","Test":"Test_keyLessThan/a\\u003cb","Elapsed":0}
{"Time":"2019-01-23T17:06:20.437755604+01:00","Action":"output","Package":"github.com/apache/beam/sdks/go/pkg/beam/io/datastoreio","Test":"Test_keyLessThan/b\\u003ea","Output":"    --- PASS: Test_keyLessThan/b\\u003ea (0.00s)\\n"}
{"Time":"2019-01-23T17:06:20.437760812+01:00","Action":"pass","Package":"github.com/apache/beam/sdks/go/pkg/beam/io/datastoreio","Test":"Test_keyLessThan/b\\u003ea","Elapsed":0}
{"Time":"2019-01-23T17:06:20.437765648+01:00","Action":"output","Package":"github.com/apache/beam/sdks/go/pkg/beam/io/datastoreio","Test":"Test_keyLessThan/a=a","Output":"    --- PASS: Test_keyLessThan/a=a (0.00s)\\n"}
{"Time":"2019-01-23T17:06:20.437770428+01:00","Action":"pass","Package":"github.com/apache/beam/sdks/go/pkg/beam/io/datastoreio","Test":"Test_keyLessThan/a=a","Elapsed":0}
{"Time":"2019-01-23T17:06:20.437777869+01:00","Action":"output","Package":"github.com/apache/beam/sdks/go/pkg/beam/io/datastoreio","Test":"Test_keyLessThan/a.a\\u003cb","Output":"    --- PASS: Test_keyLessThan/a.a\\u003cb (0.00s)\\n"}
{"Time":"2019-01-23T17:06:20.437783338+01:00","Action":"pass","Package":"github.com/apache/beam/sdks/go/pkg/beam/io/datastoreio","Test":"Test_keyLessThan/a.a\\u003cb","Elapsed":0}
{"Time":"2019-01-23T17:06:20.437795337+01:00","Action":"output","Package":"github.com/apache/beam/sdks/go/pkg/beam/io/datastoreio","Test":"Test_keyLessThan/a.a\\u003ca.b","Output":"    --- PASS: Test_keyLessThan/a.a\\u003ca.b (0.00s)\\n"}
{"Time":"2019-01-23T17:06:20.43780042+01:00","Action":"pass","Package":"github.com/apache/beam/sdks/go/pkg/beam/io/datastoreio","Test":"Test_keyLessThan/a.a\\u003ca.b","Elapsed":0}
{"Time":"2019-01-23T17:06:20.437804962+01:00","Action":"output","Package":"github.com/apache/beam/sdks/go/pkg/beam/io/datastoreio","Test":"Test_keyLessThan/a.b\\u003ea.a","Output":"    --- PASS: Test_keyLessThan/a.b\\u003ea.a (0.00s)\\n"}
{"Time":"2019-01-23T17:06:20.4378098+01:00","Action":"pass","Package":"github.com/apache/beam/sdks/go/pkg/beam/io/datastoreio","Test":"Test_keyLessThan/a.b\\u003ea.a","Elapsed":0}
{"Time":"2019-01-23T17:06:20.43781812+01:00","Action":"output","Package":"github.com/apache/beam/sdks/go/pkg/beam/io/datastoreio","Test":"Test_keyLessThan/a.a=a.a","Output":"    --- PASS: Test_keyLessThan/a.a=a.a (0.00s)\\n"}
{"Time":"2019-01-23T17:06:20.437823309+01:00","Action":"pass","Package":"github.com/apache/beam/sdks/go/pkg/beam/io/datastoreio","Test":"Test_keyLessThan/a.a=a.a","Elapsed":0}
{"Time":"2019-01-23T17:06:20.437827724+01:00","Action":"output","Package":"github.com/apache/beam/sdks/go/pkg/beam/io/datastoreio","Test":"Test_keyLessThan/4dda\\u003cA","Output":"    --- PASS: Test_keyLessThan/4dda\\u003cA (0.00s)\\n"}
{"Time":"2019-01-23T17:06:20.437832554+01:00","Action":"pass","Package":"github.com/apache/beam/sdks/go/pkg/beam/io/datastoreio","Test":"Test_keyLessThan/4dda\\u003cA","Elapsed":0}
{"Time":"2019-01-23T17:06:20.437836563+01:00","Action":"pass","Package":"github.com/apache/beam/sdks/go/pkg/beam/io/datastoreio","Test":"Test_keyLessThan","Elapsed":0}
{"Time":"2019-01-23T17:06:20.437860549+01:00","Action":"output","Package":"github.com/apache/beam/sdks/go/pkg/beam/io/datastoreio","Output":"PASS\\n"}
{"Time":"2019-01-23T17:06:20.438077604+01:00","Action":"output","Package":"github.com/apache/beam/sdks/go/pkg/beam/io/datastoreio","Output":"coverage: 17.2% of statements\\n"}
{"Time":"2019-01-23T17:06:20.4396444+01:00","Action":"output","Package":"github.com/apache/beam/sdks/go/pkg/beam/io/datastoreio","Output":"ok  \\tgithub.com/apache/beam/sdks/go/pkg/beam/io/datastoreio\\t0.018s\\tcoverage: 17.2% of statements\\n"}
{"Time":"2019-01-23T17:06:20.439676579+01:00","Action":"pass","Package":"github.com/apache/beam/sdks/go/pkg/beam/io/datastoreio","Elapsed":0.018}
'''
        // when
        PackageTestResult context = PackageTestResult.builder()
                .withPackagePath('github.com/apache/beam/sdks/go/pkg/beam/io')
                .withStdout(stdout.split(/\n/) as List)
                .withTestFiles(resource.listFiles() as List)
                .build()
        List<TestClassResult> results = extractor.extractTestResult(context)

        // then
        assert results.size() == 1

        assert results[0].className == 'github_DOT_com.apache.beam.sdks.go.pkg.beam.io.datastore_test_DOT_go'
        assert results[0].results.size() == 9

        assert results[0].results.collect { it.name } == [
                'Test_keyLessThan',
                'Test_keyLessThan/a<b',
                'Test_keyLessThan/b>a',
                'Test_keyLessThan/a=a',
                'Test_keyLessThan/a.a<b',
                'Test_keyLessThan/a.a<a.b',
                'Test_keyLessThan/a.b>a.a',
                'Test_keyLessThan/a.a=a.a',
                'Test_keyLessThan/4dda<A'
        ]
        assert results[0].results.every { it.resultType == TestResult.ResultType.SUCCESS }
    }
}
