package com.github.blindpirate.gogradle.core.dependency.produce

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.BuildConstraintManager
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
class GoImportExtractorTest {
    @Mock
    BuildConstraintManager manager

    GoImportExtractor extractor

    @Before
    void setUp() {
        extractor = new GoImportExtractor(manager)
        when(manager.getAllConstraints()).thenReturn([] as Set)
    }

    @Test
    void 'single import should be extracted correctly'() {
        assert extractor.extract('''
package main

import "fmt"

// Send the sequence 2， 3， 4， … to channel 'ch'.
func generate(ch chan<- int) {
  for i := 2; ; i++ {
    ch <- i  // Send 'i' to channel 'ch'.
  }
}
''') == ['fmt']
    }

    @Test
    void 'source code with package in comment should be parsed correctly'() {
        assert extractor.extract('''
// package main
// this is package main
package main

import "fmt"

func Main(){}
''') == ['fmt']
    }


    @Test
    void 'multiple import should be extracted correctly'() {
        assert extractor.extract('''
package main

import (
    "fmt"
    )
import (
    "math"
)

func main() {
    fmt.Printf("Now you have %g problems.", math.Sqrt(7))
}
''') == ['fmt', 'math']
    }

    @Test
    void 'special imports should be extracted correctly'() {
        assert extractor.extract('''
package main

import   "lib/math1"
import M "lib/math2"
import . "lib/math3"
import _ "lib/math4"

balabalabala
''') == ['lib/math1', 'lib/math2', 'lib/math3', 'lib/math4']
    }

    @Test
    void 'import with raw string literal should be extracted correctly'() {
        assert extractor.extract('''
package main

import   "lib/math1"
import M `lib/math2`
import . `lib/math3`
import _ `lib/math4`

balabalabala
''') == ['lib/math1', 'lib/math2', 'lib/math3', 'lib/math4']
    }

    @Test
    void 'syntax error in source code should be ignored'() {
        assert extractor.extract('''such a mess''') == []
    }


    @Test
    void 'a single build constraint should take effect'() {
        // given
        String buildTags = '''
/*redundant comment*/
// redundant comment
// package main is a special package
// +build appengine // redundant comment 
'''
        // then
        assert !evaluateBuildTags(buildTags, [])
        assert evaluateBuildTags(buildTags, ['appengine'])
    }

    @Test
    void 'a single negative build constraint should take effect'() {
        // given
        String buildTags = '''
/*redundant comment*/
// redundant comment
// +build !appengine /* redundant 
comment*/
'''
        // then
        assert evaluateBuildTags(buildTags, [])
        assert !evaluateBuildTags(buildTags, ['appengine'])
    }

    @Test
    void 'many NOT should be evaluated correctly'() {
        // given
        // (NOT linux AND NOT darwin) OR (NOT cgo)
        String buildTags = '// +build !linux,!darwin !cgo'
        // then
        assert evaluateBuildTags(buildTags, [])
        assert evaluateBuildTags(buildTags, ['linux'])
        assert !evaluateBuildTags(buildTags, ['linux', 'cgo'])
        assert evaluateBuildTags(buildTags, ['cgo'])
        assert !evaluateBuildTags(buildTags, ['linux', 'darwin', 'cgo'])
    }

    @Test
    void 'a single complicated build constraint should take effect'() {
        // should be treated as (linux AND 386) OR (darwin AND (NOT cgo))
        // given
        String buildTags = '// +build linux,386 darwin,!cgo'
        // then
        assert !evaluateBuildTags(buildTags, [])
        assert evaluateBuildTags(buildTags, ['darwin'])
        assert !evaluateBuildTags(buildTags, ['linux'])
        assert !evaluateBuildTags(buildTags, ['386'])
        assert !evaluateBuildTags(buildTags, ['darwin', 'cgo'])
    }

    @Test
    void 'mutiple build constraints should take effect'() {
        // given
        // should be treated as (linux OR darwin) AND 386
        String buildTags = '''
// +build linux darwin
// +build 386
'''
        // then
        assert !evaluateBuildTags(buildTags, [])
        assert evaluateBuildTags(buildTags, ['linux', '386'])
        assert evaluateBuildTags(buildTags, ['darwin', '386'])
    }

    @Test(expected = IllegalStateException)
    void '!!term should cause an exception'() {
        // given
        String buildTags = '''
// +build !!term
'''
        // then
        evaluateBuildTags(buildTags, [])
    }

    boolean evaluateBuildTags(String buildTags, List ctx) {
        when(manager.getAllConstraints()).thenReturn(ctx as Set)
        String code = buildTags + '''
package main
import "fmt"
'''
        return !extractor.extract(code).isEmpty()
    }

}
