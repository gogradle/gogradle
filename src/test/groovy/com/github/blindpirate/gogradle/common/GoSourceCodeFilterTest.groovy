package com.github.blindpirate.gogradle.common

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.util.IOUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

import static com.github.blindpirate.gogradle.common.GoSourceCodeFilter.SourceSetType.*
import static com.github.blindpirate.gogradle.common.GoSourceCodeFilter.filterGoFiles
import static com.github.blindpirate.gogradle.common.GoSourceCodeFilter.filterTestsMatchingPattern

@RunWith(GogradleRunner)
@WithResource()
class GoSourceCodeFilterTest {
    File resource

    @Before
    void setUp() {
        IOUtils.write(resource, '.hidden/any.go', '')
        IOUtils.write(resource, '_hidden/any_test.go', '')

        IOUtils.write(resource, 'a/a1_test.go', '')
        IOUtils.write(resource, 'a/nogo', '')
        IOUtils.write(resource, 'a/_a1_test.go', '')
        IOUtils.write(resource, 'a/a2_test.go', '')
        IOUtils.write(resource, 'a/.a2_test.go', '')
        IOUtils.write(resource, 'a/a1.go', '')
        IOUtils.write(resource, 'a/a2.go', '')

        IOUtils.write(resource, 'b/b1_test.go', '')
        IOUtils.write(resource, 'b/b2_test.go', '')
        IOUtils.write(resource, 'b/b1.go', '')
        IOUtils.write(resource, 'b/b2.go', '')

        IOUtils.write(resource, 'c/testdata/c_test.go', '')
        IOUtils.write(resource, 'c/c.go', '')
        IOUtils.write(resource, 'c/c.nongo', '')

        IOUtils.mkdir(resource, 'd')
        IOUtils.mkdir(resource, 'vendor')
        IOUtils.write(resource, 'vendor/sub/vendor.go', '')
        IOUtils.write(resource, 'vendor/sub/vendor_test.go', '')
        IOUtils.write(resource, 'vendor/_vendor.go', '')
        IOUtils.write(resource, 'vendor/.vendor_test.go', '')
        IOUtils.write(resource, 'vendor/nogo', '')

        IOUtils.write(resource, 'e/vendor/.evendor.go', '')
        IOUtils.write(resource, 'e/vendor/evendor.go', '')
        IOUtils.write(resource, 'f/vendor/fvendor_test.go', '')
    }

    @Test
    void 'can get build files only'() {
        def expectedResult = ['a1.go', 'a2.go', 'b1.go', 'b2.go', 'c.go', 'evendor.go'] as Set
        assert filterGoFiles(resource, PROJECT_BUILD_FILES_ONLY)*.name as Set == expectedResult
        assert filterGoFiles(resource, 'build')*.name as Set == expectedResult
    }

    @Test
    void 'can get test files only'() {
        def expectedResult = ['a1_test.go', 'a2_test.go', 'b1_test.go', 'b2_test.go', 'fvendor_test.go'] as Set
        assert filterGoFiles(resource, PROJECT_TEST_FILES_ONLY)*.name as Set == expectedResult
        assert filterGoFiles(resource, 'test')*.name as Set == expectedResult
    }

    @Test
    void 'can get build files along with vendor build files'() {
        def result = ['a1.go', 'a2.go', 'b1.go', 'b2.go', 'c.go', 'evendor.go', 'vendor.go'] as Set
        assert filterGoFiles(resource, PROJECT_AND_VENDOR_BUILD_FILES)*.name as Set == result
    }

    @Test
    void 'can get test files along with vendor build files'() {
        def result = ['a1.go', 'a2.go', 'b1.go', 'b2.go', 'c.go', 'evendor.go', 'a1_test.go', 'a2_test.go', 'b1_test.go', 'b2_test.go', 'fvendor_test.go', 'vendor.go'] as Set
        assert filterGoFiles(resource, PROJECT_TEST_AND_VENDOR_BUILD_FILES)*.name as Set == result
    }

    boolean accept(String fileName, String... pattern) {
        String randomDir = UUID.randomUUID().toString()
        IOUtils.write(resource, randomDir + '/' + fileName, '')
        return filterTestsMatchingPattern(new File(resource, randomDir), pattern as List)*.name.contains(fileName)
    }

    @Test
    void 'filtering with question mark should succeed'() {
        assert !accept('main_test.go', '?')
        assert !accept('main.go', '?')
        assert !accept('_main.go', '?')
        assert !accept('.main.go', '?')
        assert !accept('.main.go', '?')

        assert !accept('main_test.go', 'main_?')
        assert accept('main_test.go', 'main_test.g?')
        assert accept('main_test.go', 'm??n_t??t.g?')
    }

    @Test
    void 'filtering with star should succeed'() {
        assert accept('main_test.go', '*')
        assert !accept('main.go', '*')
        assert !accept('_main.go', '*')
        assert !accept('.main.go', '*')
        assert !accept('.main.go', '*')

        assert accept('main_test.go', 'main_*')
        assert !accept('main_test.go', 'main1_*')
        assert accept('main_test.go', 'main*_test.go')
        assert accept('main_test.go', 'main*test.go')
        assert accept('main_test.go', '*main*t*s*t.go')
    }

    @Test
    void 'joint filtering should succeed'() {
        assert accept('main_test.go', '*', '?')
        assert !accept('main.go', '*', '?')
        assert !accept('_main.go', '*', '?')
        assert !accept('.main.go', '*', '?')
        assert !accept('.main.go', '*', '?')
    }
}