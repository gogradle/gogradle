package com.github.blindpirate.gogradle.build

import org.junit.Test

class TestPatternFilterTest {
    @Test
    void 'filtering with question mark should succeed'() {
        assert !filter('?').accept(null, 'main_test.go')
        assert !filter('?').accept(null, 'main.go')
        assert !filter('?').accept(null, '_main.go')
        assert !filter('?').accept(null, '.main.go')
        assert !filter('?').accept(new File('.main.go'))

        assert !filter('main_?').accept(null, 'main_test.go')
        assert filter('main_test.g?').accept(null, 'main_test.go')
        assert filter('m??n_t??t.g?').accept(null, 'main_test.go')
    }

    @Test
    void 'filtering with star should succeed'() {
        assert filter('*').accept(null, 'main_test.go')
        assert !filter('*').accept(null, 'main.go')
        assert !filter('*').accept(null, '_main.go')
        assert !filter('*').accept(null, '.main.go')
        assert !filter('*').accept(new File('.main.go'))

        assert filter('main_*').accept(null, 'main_test.go')
        assert !filter('main1_*').accept(null, 'main_test.go')
        assert filter('main*_test.go').accept(null, 'main_test.go')
        assert filter('main*test.go').accept(null, 'main_test.go')
        assert filter('*main*t*s*t.go').accept(null, 'main_test.go')
    }

    @Test
    void 'joint filtering should succeed'() {
        assert filter(['*', '?']).accept(null, 'main_test.go')
        assert !filter(['*', '?']).accept(null, 'main.go')
        assert !filter(['*', '?']).accept(null, '_main.go')
        assert !filter(['*', '?']).accept(null, '.main.go')
        assert !filter(['*', '?']).accept(new File('.main.go'))
    }

    TestPatternFilter filter(String pattern) {
        return new TestPatternFilter([pattern])
    }

    TestPatternFilter filter(List<String> patterns) {
        return new TestPatternFilter(patterns)
    }

}
