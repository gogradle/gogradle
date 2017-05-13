package com.github.blindpirate.gogradle.common

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.util.IOUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

import java.util.function.Predicate

@RunWith(GogradleRunner)
@WithResource('')
class MarkAndDeleteDirectoryVisitorTest {
    File resource

    /*

     resource
        |---- a (ancestor of marked)
        |     |-- c
        |     \-- d (ancestor of marked)
        |         \-- f (marked)
        |             \-- g
        \-- b
            \-- e
     */

    @Before
    void setUp() {
        IOUtils.mkdir(resource, 'a/c')
        IOUtils.mkdir(resource, 'a/d/f/g')
        IOUtils.mkdir(resource, 'b/e')
    }

    @Test
    void 'marking should succeed'() {
        MarkDirectoryVisitor visitor = mark()
        assert visitor.markedDirectories == dirSet('a/d/f')
        assert visitor.ancestorsOfMarkedDirectories == dirSet('a', 'a/d', '')
    }

    private MarkDirectoryVisitor mark() {
        MarkDirectoryVisitor visitor = new MarkDirectoryVisitor(new Predicate<File>() {
            @Override
            boolean test(File file) {
                return 'f' == file.name
            }
        }, resource)

        IOUtils.walkFileTreeSafely(resource.toPath(), visitor)
        return visitor
    }


    @Test
    void 'deleting marked dir should succeed'() {
        DeleteUnmarkedDirectoryVistor visitor = new DeleteUnmarkedDirectoryVistor(mark())
        IOUtils.walkFileTreeSafely(resource.toPath(), visitor)
        assert new File(resource, 'a/d/f/g').exists()
        assert !new File(resource, 'a/c').exists()
        assert !new File(resource, 'b/e').exists()
    }

    Set dirSet(String... dirs) {
        return dirs.collect { new File(resource, it) } as Set
    }
}
