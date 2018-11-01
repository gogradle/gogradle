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

package com.github.blindpirate.gogradle.task.go

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.util.IOUtils
import org.gradle.api.internal.tasks.testing.junit.result.TestClassResult
import org.gradle.api.tasks.testing.TestResult
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(GogradleRunner)
@WithResource('')
class GoTestStdoutExtractorTest {

    GoTestStdoutExtractor extractor = new GoTestStdoutExtractor()

    File resource

    @Test
    void 'extracting one result should succeed'() {
        // given
        IOUtils.write(resource, 'a_test.go', 'func Test_A()')
        String stdout = '''\
=== RUN   Test_A
--- FAIL: Test_A (0.00s)
FAIL
exit status 
FAIL\ta\t0.006s'''
        // when
        PackageTestResult context = PackageTestResult.builder()
                .withPackagePath('a/b/c')
                .withStdout(stdout.split(/\n/) as List)
                .withTestFiles(resource.listFiles() as List)
                .build()
        List<TestClassResult> results = extractor.extractTestResult(context)

        assert results.size() == 1

        TestClassResult result = results[0]
        assert result.className == 'a.b.c.a_test_DOT_go'
        assert result.results.size() == 1

        assert result.results[0].name == 'Test_A'
        assert result.results[0].message.trim().contains('Test_A')
        assert result.results[0].resultType == TestResult.ResultType.FAILURE
    }

    @Test
    void 'extracting Skip result should succeed'() {
        // given
        IOUtils.write(resource, 'a_test.go', 'func Test_A()')
        String stdout = '''\
=== RUN   Test_A
--- SKIP: Test_A (0.00s)
PASS
'''
        // when
        PackageTestResult context = PackageTestResult.builder()
                .withPackagePath('a/b/c')
                .withStdout(stdout.split(/\n/) as List)
                .withTestFiles(resource.listFiles() as List)
                .build()
        List<TestClassResult> results = extractor.extractTestResult(context)

        assert results.size() == 1

        TestClassResult result = results[0]
        assert result.className == 'a.b.c.a_test_DOT_go'
        assert result.results.size() == 1

        assert result.results[0].name == 'Test_A'
        assert result.results[0].message.trim().contains('Test_A')
        assert result.results[0].resultType == TestResult.ResultType.SKIPPED
    }

    @Test
    void 'extracting Log/Error output of testing.T should succeed'() {
        // given
        IOUtils.write(resource, 'a_test.go', 'func Test_A1() func Test_A2() func Test_A3()')
        String stdout = '''\
=== RUN   Test_A1
--- FAIL: Test_A1 (0.00s)
\ta1_test.go:9: Failed
=== RUN   Test_A2
SomeOutputWithoutNewline--- PASS: Test_A2 (0.00s)
\ta1_test.go:15: Passed
=== RUN   Test_A3
--- PASS: Test_A3 (0.00s)
\ta2_test.go:7: Passed
FAIL
coverage: 66.7% of statements
exit status 1
FAIL\tgithub.com/my/project/a\t0.006s'''
        // when
        PackageTestResult context = PackageTestResult.builder()
                .withPackagePath('a/b/c')
                .withStdout(stdout.split(/\n/) as List)
                .withTestFiles(resource.listFiles() as List)
                .build()
        List<TestClassResult> results = extractor.extractTestResult(context)

        assert results.size() == 1

        TestClassResult result = results[0]
        assert result.className == 'a.b.c.a_test_DOT_go'
        assert result.results.size() == 3

        assert result.results[0].name == 'Test_A1'
        assert result.results[0].message.trim().contains('a1_test.go:9: Failed')
        assert result.results[0].resultType == TestResult.ResultType.FAILURE

        assert result.results[1].name == 'Test_A2'
        assert result.results[1].message.trim().contains('SomeOutputWithoutNewline')
        assert result.results[1].message.trim().contains('a1_test.go:15: Passed')
        assert result.results[1].resultType == TestResult.ResultType.SUCCESS

        assert result.results[2].name == 'Test_A3'
        assert result.results[2].message.trim().contains('a2_test.go:7: Passed')
        assert result.results[2].resultType == TestResult.ResultType.SUCCESS
    }

    @Test
    void 'extracting results from stdout should succeed'() {
        // given
        IOUtils.write(resource, 'a_test.go', 'func TestDiffToHTML()')
        IOUtils.write(resource, 'b_test.go', 'func Test_parsePostgreSQLHostPort() func Test_SSHParsePublicKey()')
        IOUtils.write(resource, 'c.c_test.go', 'func TestRepo()')
        IOUtils.write(resource, 'd_test.go', 'func Useless()')
        String stdout = '''\
2017/03/13 16:05:18 \u001B[1;33m[W] Custom config '/var/folders/y2/gy07kxsn58jdd97b43jcch040000gn/T/go-build158292335/github.com/gogits/gogs/models/_test/custom/conf/app.ini' not found, ignore this if you're running first time
=== RUN   TestDiffToHTML
--- PASS: TestDiffToHTML (0.00s)
=== RUN   Test_parsePostgreSQLHostPort

  Parse PostgreSQL host and port ✔✔✔✔✔✔✔✔✔✔✔✔


12 total assertions

--- PASS: Test_parsePostgreSQLHostPort (0.00s)
=== RUN   Test_SSHParsePublicKey

  Parse public keys in both native and ssh-keygen 
Testing key: ecdsa-256
✔✔✔✔✔
Testing key: ecdsa-384
✔✔✔✔✔
Testing key: dsa-1024
✔✔✘


Failures:

  * /Users/zhb/Develop/top1000Go/gogits_gogs/.gogradle/project_gopath/src/github.com/gogits/gogs/models/ssh_key_test.go 
  Line 42:
  Expected: '10241\'
  Actual:   '1024\'
  (Should be equal)


25 total assertions

--- FAIL: Test_SSHParsePublicKey (0.04s)
=== RUN   TestRepo

  The metas map 
    When no external tracker is configured 
      It should be nil ✔
      It should be nil even if other settings are present ✔
    When an external issue tracker is configured 
      It should default to numeric issue style ✔
      It should pass through numeric issue style setting ✔
      It should pass through alphanumeric issue style setting ✔
      It should contain the user name ✔
      It should contain the repo name ✔
      It should contain the URL format ✔


33 total assertions

--- PASS: TestRepo (0.00s)
FAIL
exit status 1
FAIL\tgithub.com/gogits/gogs/models\t0.074s'''
        // when
        PackageTestResult context = PackageTestResult.builder()
                .withPackagePath('github.com/gogits/gogs/models')
                .withStdout(stdout.split(/\n/) as List)
                .withTestFiles(resource.listFiles() as List)
                .build()
        // Sort to ensure the order
        List<TestClassResult> results = extractor.extractTestResult(context).sort({ result1, result2 ->
            result1.className <=> result2.className
        })

        // then
        assert results.size() == 3
        assert results.unique({ it.id }).size() == 3

        assert results[0].className == 'github_DOT_com.gogits.gogs.models.a_test_DOT_go'
        assert results[0].results.size() == 1
        assert results[0].results[0].name == 'TestDiffToHTML'
        assert results[0].results[0].message.contains('TestDiffToHTML')
        assert results[0].results[0].resultType == TestResult.ResultType.SUCCESS

        assert results[1].className == 'github_DOT_com.gogits.gogs.models.b_test_DOT_go'
        assert results[1].results.size() == 2
        assert results[1].results.unique { it.id }.size() == 2
        assert results[1].results[0].name == 'Test_parsePostgreSQLHostPort'
        assert results[1].results[0].resultType == TestResult.ResultType.SUCCESS
        assert results[1].results[0].message.contains('Parse PostgreSQL host and port')
        assert results[1].results[1].name == 'Test_SSHParsePublicKey'
        assert results[1].results[1].resultType == TestResult.ResultType.FAILURE
        assert results[1].results[1].message.contains('Failures:')

        assert results[2].className == 'github_DOT_com.gogits.gogs.models.c_DOT_c_test_DOT_go'
        assert results[2].results.size() == 1
        assert results[2].results[0].name == 'TestRepo'
        assert results[2].results[0].message.contains('33 total assertions')
        assert results[2].results[0].resultType == TestResult.ResultType.SUCCESS
    }

    @Test
    void 'extracting results from stdout with [setup failed] should succeed'() {
        // given
        String stdout = '''\
# github.com/gogits/gogs/models
.gogradle/project_gopath/src/github.com/gogits/gogs/models/ssh_key_test.go:7:1: expected declaration, found 'IDENT' sfds
FAIL\tgithub.com/gogits/gogs/models [setup failed]'''
        // when
        PackageTestResult context = PackageTestResult.builder()
                .withPackagePath('github.com/gogits/gogs/models')
                .withStdout(stdout.split(/\n/) as List)
                .withTestFiles([])
                .build()
        List<TestClassResult> results = extractor.extractTestResult(context)
        // then
        assert results.size() == 1
        assert results[0].className == 'github_DOT_com.gogits.gogs.models.[setup failed]'
        assert results[0].results.size() == 1
        assert results[0].results[0].name == '[setup failed]'
        assert results[0].results[0].message.contains('github.com/gogits/gogs/models [setup failed]')
        assert results[0].results[0].resultType == TestResult.ResultType.FAILURE
    }

    @Test
    void 'extracting results from stdout with [build failed] should succeed'() {
        // given
        String stdout = '''\
# a
./a1.go:3: syntax error: non-declaration statement outside function body
FAIL\ta [build failed]'''
        // when
        PackageTestResult context = PackageTestResult.builder()
                .withPackagePath('a')
                .withStdout(stdout.split(/\n/) as List)
                .withTestFiles([])
                .build()
        List<TestClassResult> results = extractor.extractTestResult(context)
        // then
        assert results.size() == 1
        assert results[0].className == 'a.[build failed]'
        assert results[0].results.size() == 1
        assert results[0].results[0].name == '[build failed]'
        assert results[0].results[0].message.contains('syntax error')
        assert results[0].results[0].message.contains('a [build failed]')
        assert results[0].results[0].resultType == TestResult.ResultType.FAILURE
    }

    @Test
    void 'extracting results from stdout when package cannot be loaded should succeed'() {
        // given
        String stdout = '''\
can't load package: package github.com/my/project/b: found packages broken (b1.go) and b (b1_test.go) in /src/github.com/my/project/b
'''
        // when
        PackageTestResult context = PackageTestResult.builder()
                .withPackagePath('b')
                .withStdout(stdout.split(/\n/) as List)
                .withTestFiles([])
                .build()
        List<TestClassResult> results = extractor.extractTestResult(context)
        // then
        assert results.size() == 1
        assert results[0].className == "b.can't load package"
        assert results[0].results.size() == 1
        assert results[0].results[0].name == "can't load package"
        assert results[0].results[0].message.contains('found packages broken')
        assert results[0].results[0].resultType == TestResult.ResultType.FAILURE
    }

    @Test
    void 'extracting results from stdout when package cannot be found should succeed'() {
        // given
        String stdout = '''\
cannot find package: package github.com/my/project/b: found packages broken (b1.go) and b (b1_test.go) in /src/github.com/my/project/b
'''
        // when
        PackageTestResult context = PackageTestResult.builder()
                .withPackagePath('b')
                .withStdout(stdout.split(/\n/) as List)
                .withTestFiles([])
                .build()
        List<TestClassResult> results = extractor.extractTestResult(context)
        // then
        assert results.size() == 1
        assert results[0].className == "b.cannot find package"
        assert results[0].results.size() == 1
        assert results[0].results[0].name == "cannot find package"
        assert results[0].results[0].message.contains('found packages broken')
        assert results[0].results[0].resultType == TestResult.ResultType.FAILURE
    }

    @Test
    void 'extracting result with panic should succeed'() {
        // given
        String stdout = '''\
panic: 42

goroutine 1 [running]:
github.com/my/package.init.1()
\t/Users/zhb/Projects/examples/typical-build/.gogradle/project_gopath/src/github.com/my/package/StringReverse.go:9 +0x58
github.com/my/package.init()
\t/Users/zhb/Projects/examples/typical-build/.gogradle/project_gopath/src/github.com/my/package/StringReverse_test.go:8 +0x4e
main.init()
\tgithub.com/my/package/_test/_testmain.go:46 +0x53
exit status 2
FAIL\tgithub.com/my/package\t0.010s
'''
        // when
        PackageTestResult result = PackageTestResult.builder()
                .withPackagePath('a/b/c')
                .withStdout(stdout.split(/\n/) as List)
                .withTestFiles(resource.listFiles() as List)
                .withCode(2)
                .build()
        List<TestClassResult> results = extractor.extractTestResult(result)

        assert results.size() == 1
        assert results[0].className == "a.b.c.go test return 2"
        assert results[0].results.size() == 1
        assert results[0].results[0].name == "go test return 2"
        assert results[0].results[0].message.contains('panic: 42')
        assert results[0].results[0].resultType == TestResult.ResultType.FAILURE
    }

    // https://github.com/gogradle/gogradle/issues/112
    @Test
    void 'extracting result with goroutine panic should succeed'() {
        // given
        String stdout = '''\
=== RUN   Test_SingleKeyerSetGet
demoStruct &{<nil>}
panic: interface conversion: interface {} is nil, not *demo.testStr

goroutine 6 [running]:
demo.Test_SingleKeyerSetGet.func1()
\t/Users/zhb/go/src/demo/demo_test.go:32 +0x154
created by demo.Test_SingleKeyerSetGet
\t/Users/zhb/go/src/demo/demo_test.go:33 +0x39
exit status 2
FAIL\tdemo\t0.008s
'''
        new File(resource, 'a_test.go').write('func Test_SingleKeyerSetGet()')
        // when
        PackageTestResult result = PackageTestResult.builder()
                .withPackagePath('a/b/c')
                .withStdout(stdout.split(/\n/) as List)
                .withTestFiles(resource.listFiles() as List)
                .withCode(2)
                .build()
        List<TestClassResult> results = extractor.extractTestResult(result)

        assert results.size() == 1
        assert results[0].className == "a.b.c.a_test_DOT_go"
        assert results[0].results.size() == 1
        assert results[0].results[0].name == "Test_SingleKeyerSetGet"
        assert results[0].results[0].message.contains('panic:')
        assert results[0].results[0].resultType == TestResult.ResultType.FAILURE
    }

    @Test
    void 'extracting stdout which can cause stack overflow should succeed'() {
        // given
        String stdout = '''\
=== RUN   TestMarkdown

  Rendering an issue mention 
    To the internal issue tracker 
      It should not render anything when there are no mentions \u001B[32m✔\u001B[0m\u001B[32m✔\u001B[0m\u001B[32m✔\u001B[0m\u001B[32m✔\u001B[0m\u001B[32m✔\u001B[0m\u001B[32m✔\u001B[0m\u001B[32m✔\u001B[0m\u001B[32m✔\u001B[0m\u001B[32m✔\u001B[0m\u001B[32m✔\u001B[0m\u001B[32m✔\u001B[0m
      It should render freestanding mentions \u001B[32m✔\u001B[0m\u001B[32m✔\u001B[0m\u001B[32m✔\u001B[0m\u001B[32m✔\u001B[0m\u001B[32m✔\u001B[0m\u001B[32m✔\u001B[0m
      It should not render issue mention without leading space \u001B[32m✔\u001B[0m
      It should not render issue mention without trailing space \u001B[32m✔\u001B[0m
      It should render issue mention in parentheses \u001B[32m✔\u001B[0m\u001B[32m✔\u001B[0m\u001B[32m✔\u001B[0m\u001B[32m✔\u001B[0m\u001B[32m✔\u001B[0m
      It should render multiple issue mentions in the same line \u001B[32m✔\u001B[0m\u001B[32m✔\u001B[0m\u001B[32m✔\u001B[0m\u001B[32m✔\u001B[0m\u001B[32m✔\u001B[0m\u001B[32m✔\u001B[0m
    To an external issue tracker with numeric style 
      should not render anything when there are no mentions \u001B[32m✔\u001B[0m\u001B[32m✔\u001B[0m\u001B[32m✔\u001B[0m\u001B[32m✔\u001B[0m\u001B[32m✔\u001B[0m\u001B[32m✔\u001B[0m
      It should render freestanding issue mentions \u001B[32m✔\u001B[0m\u001B[32m✔\u001B[0m\u001B[32m✔\u001B[0m\u001B[32m✔\u001B[0m\u001B[32m✔\u001B[0m\u001B[32m✔\u001B[0m
      It should not render issue mention without leading space \u001B[32m✔\u001B[0m
      It should not render issue mention without trailing space \u001B[32m✔\u001B[0m
      It should render issue mention in parentheses \u001B[32m✔\u001B[0m\u001B[32m✔\u001B[0m\u001B[32m✔\u001B[0m\u001B[32m✔\u001B[0m\u001B[32m✔\u001B[0m
      It should render multiple issue mentions in the same line \u001B[32m✔\u001B[0m\u001B[32m✔\u001B[0m\u001B[32m✔\u001B[0m\u001B[32m✔\u001B[0m\u001B[32m✔\u001B[0m\u001B[32m✔\u001B[0m
    To an external issue tracker with alphanumeric style 
      It should not render anything when there are no mentions \u001B[32m✔\u001B[0m\u001B[32m✔\u001B[0m\u001B[32m✔\u001B[0m\u001B[32m✔\u001B[0m\u001B[32m✔\u001B[0m\u001B[32m✔\u001B[0m\u001B[32m✔\u001B[0m\u001B[32m✔\u001B[0m\u001B[32m✔\u001B[0m
      It should render freestanding issue mention \u001B[32m✔\u001B[0m\u001B[32m✔\u001B[0m\u001B[32m✔\u001B[0m\u001B[32m✔\u001B[0m\u001B[32m✔\u001B[0m\u001B[32m✔\u001B[0m
      It should render issue mention in parentheses \u001B[32m✔\u001B[0m\u001B[32m✔\u001B[0m\u001B[32m✔\u001B[0m\u001B[32m✔\u001B[0m\u001B[32m✔\u001B[0m
      It should render multiple issue mentions in the same line \u001B[32m✔\u001B[0m\u001B[32m✔\u001B[0m\u001B[32m✔\u001B[0m\u001B[32m✔\u001B[0m\u001B[32m✔\u001B[0m\u001B[32m✔\u001B[0m

\u001B[31m\u001B[0m\u001B[33m\u001B[0m\u001B[32m
81 total assertions\u001B[0m


  Rendering an issue URL 
    To the internal issue tracker 
      It should render valid issue URLs \u001B[32m✔\u001B[0m
      It should render but not change non-issue URLs \u001B[32m✔\u001B[0m\u001B[32m✔\u001B[0m\u001B[32m✔\u001B[0m\u001B[32m✔\u001B[0m\u001B[32m✔\u001B[0m\u001B[32m✔\u001B[0m

\u001B[31m\u001B[0m\u001B[33m\u001B[0m\u001B[32m
88 total assertions\u001B[0m


  Rendering a commit URL 
    To the internal issue tracker 
      It should correctly convert URLs \u001B[32m✔\u001B[0m\u001B[32m✔\u001B[0m\u001B[32m✔\u001B[0m\u001B[32m✔\u001B[0m

\u001B[31m\u001B[0m\u001B[33m\u001B[0m\u001B[32m
92 total assertions\u001B[0m

--- PASS: TestMarkdown (0.00s)
'''
        IOUtils.write(resource, 'gogs_test.go', 'func TestMarkdown()')

        // when
        PackageTestResult context = PackageTestResult.builder()
                .withPackagePath('gogs')
                .withStdout(stdout.split(/\n/) as List)
                .withTestFiles(resource.listFiles() as List)
                .build()
        List<TestClassResult> results = extractor.extractTestResult(context)
        // then
        assert results.size() == 1
        assert results[0].className == "gogs.gogs_test_DOT_go"
        assert results[0].results.size() == 1
        assert results[0].results[0].name == "TestMarkdown"
        assert results[0].results[0].message.contains('Rendering a commit URL')
        assert results[0].results[0].resultType == TestResult.ResultType.SUCCESS
    }

    // https://github.com/gogradle/gogradle/issues/120
    @Test
    void 'tests containing sub tests should succeed'() {
        // given
        String stdout = '''
=== RUN   TestInstantiate
=== RUN   TestInstantiate/test:_1
=== RUN   TestInstantiate/test:_2
=== RUN   TestInstantiate/test:_3
--- PASS: TestInstantiate (0.00s)
    --- PASS: TestInstantiate/test:_1 (0.00s)
    --- PASS: TestInstantiate/test:_2 (0.00s)
    --- PASS: TestInstantiate/test:_3 (0.00s)
PASS
ok  \tdemo\t0.008s
'''
        IOUtils.write(resource, 'a_test.go', 'func TestInstantiate()')

        // when
        PackageTestResult context = PackageTestResult.builder()
                .withPackagePath('my')
                .withStdout(stdout.split(/\n/) as List)
                .withTestFiles(resource.listFiles() as List)
                .build()
        List<TestClassResult> results = extractor.extractTestResult(context)
        // then
        assert results.size() == 1
        assert results[0].className == "my.a_test_DOT_go"
        assert results[0].results.size() == 1
        assert results[0].results[0].name == "TestInstantiate"
        assert results[0].results[0].message.contains('TestInstantiate/test:_1')
        assert results[0].results[0].resultType == TestResult.ResultType.SUCCESS
    }

    @Test
    void 'tests containing sub sub tests should succeed'() {
        // given
        String stdout = '''
=== RUN   TestInstantiate
=== RUN   TestInstantiate/test:_1
=== RUN   TestInstantiate/test:_1/subsub
=== RUN   TestInstantiate/test:_2
=== RUN   TestInstantiate/test:_2/subsub
=== RUN   TestInstantiate/test:_3
=== RUN   TestInstantiate/test:_3/subsub
--- PASS: TestInstantiate (0.00s)
    --- PASS: TestInstantiate/test:_1 (0.00s)
        --- PASS: TestInstantiate/test:_1/subsub (0.00s)
    --- PASS: TestInstantiate/test:_2 (0.00s)
        --- PASS: TestInstantiate/test:_2/subsub (0.00s)
    --- PASS: TestInstantiate/test:_3 (0.00s)
        --- PASS: TestInstantiate/test:_3/subsub (0.00s)
PASS
ok  \t_/Users/zhb/Projects/tmp/gogradle120\t0.009s
'''
        IOUtils.write(resource, 'a_test.go', 'func TestInstantiate()')

        // when
        PackageTestResult context = PackageTestResult.builder()
                .withPackagePath('my')
                .withStdout(stdout.split(/\n/) as List)
                .withTestFiles(resource.listFiles() as List)
                .build()
        List<TestClassResult> results = extractor.extractTestResult(context)
        // then
        assert results.size() == 1
        assert results[0].className == "my.a_test_DOT_go"
        assert results[0].results.size() == 1
        assert results[0].results[0].name == "TestInstantiate"
        assert results[0].results[0].message.contains('TestInstantiate/test:_1')
        assert results[0].results[0].resultType == TestResult.ResultType.SUCCESS
    }
}
