package com.github.blindpirate.gogradle.core.dependency.produce

import org.junit.Test

class GoImportExtractorTest {
    GoImportExtractor extractor = new GoImportExtractor()

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
    void 'multiple import should be extracted correctly'() {
        assert extractor.extract('''
package main

import (
    "fmt"
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

}
