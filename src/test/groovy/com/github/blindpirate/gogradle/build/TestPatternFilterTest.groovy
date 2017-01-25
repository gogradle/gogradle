package com.github.blindpirate.gogradle.build

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.WithResource
import com.github.blindpirate.gogradle.util.IOUtils
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(GogradleRunner)
class TestPatternFilterTest {
    File resource

    @Test
    @WithResource('')
    void 'file or directory starting with _ or . should be rejected'() {
        IOUtils.mkdir(resource, '_dir')
        IOUtils.mkdir(resource, '.dir')
        IOUtils.write(resource, '_file', '')
        IOUtils.write(resource, '.file', '')

        assert !filter('').accept(new File(resource, '_dir'))
        assert !filter('').accept(new File(resource, '.dir'))
        assert !filter('').accept(new File(resource, '_file'))
        assert !filter('').accept(new File(resource, '.file'))
    }

    @Test
    @WithResource('')
    void 'testdata directory should be rejected'() {
        IOUtils.mkdir(resource, 'testdata')
        assert !filter('').accept(new File(resource, 'testdata'))
    }

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
