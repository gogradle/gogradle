package com.github.blindpirate.gogradle

import com.github.blindpirate.gogradle.support.IntegrationTestSupport
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.task.go.GoCoverTaskTest
import com.github.blindpirate.gogradle.util.IOUtils
import org.gradle.tooling.BuildException
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(GogradleRunner)
@WithResource('go-test-cover.zip')
class GoTestCoverIntegrationTest extends IntegrationTestSupport {
    File resource

    String buildDotGradle = """
${buildDotGradleBase}
golang {
    packagePath='github.com/my/project'
}
"""

    @Before
    void setUp() {
        writeBuildAndSettingsDotGradle(buildDotGradle)
        IOUtils.clearDirectory(new File(resource, '.gogradle'))
    }

    @Test
    void 'test and coverage report should be generated successfully'() {
        try {
            newBuild {
                it.forTasks('check')
            }
        } finally {
            println(stderr)
            println(stdout)
        }

        assert stdout.toString().contains('1 succeed, 0 failed')
        assert stdout.toString().contains('3 succeed, 0 failed')
        assert stdout.toString().contains('BUILD SUCCESSFUL')

        GoCoverTaskTest.examineCoverageHtmls(resource)
    }

    @Test
    void 'test report should be generated if test fails'() {
        new File(resource, 'a/a1.go').delete()
        new File(resource, 'a/a1.go.fail').renameTo(new File(resource, 'a/a1.go'))
        new File(resource, 'b/b1.go').delete()
        new File(resource, 'b/b1.go.fail').renameTo(new File(resource, 'b/b1.go'))
        new File(resource, 'c/c1.go.fail').renameTo(new File(resource, 'c/c1.go'))
        new File(resource, 'c/c1_test.go.fail').renameTo(new File(resource, 'c/c1_test.go'))
        new File(resource, 'd/d1.go.fail').renameTo(new File(resource, 'd/d1.go'))
        new File(resource, 'd/d1_test.go.fail').renameTo(new File(resource, 'd/d1_test.go'))

        try {
            newBuild {
                it.forTasks('check')
            }
        } catch (BuildException e) {
            println(stderr)
            println(stdout)
            assert stderr.toString().contains('There are 4 failed tests')
            examineTestReportHtmls()
        }
    }

    void examineTestReportHtmls() {
        String indexHtml = IOUtils.toString(new File(resource, '.gogradle/reports/test/index.html'))
        Document index = Jsoup.parse(indexHtml)
        // our rewrite script
        assert indexHtml.contains('h1,li>a,td>a,.breadcrumbs>a')
        assert index.select('#tests>.counter')[0].text() == '6'
        assert index.select('#failures>.counter')[0].text() == '4'

        String aHtml = IOUtils.toString(new File(resource, '.gogradle/reports/test/packages/github_DOT_com.my.project.a.html'))
        Document a = Jsoup.parse(aHtml)
        assert aHtml.contains('h1,li>a,td>a,.breadcrumbs>a')
        assert a.select('#tests>.counter')[0].text() == '3'
        assert a.select('#failures>.counter')[0].text() == '1'

        String a1Html = IOUtils.toString(new File(resource, '.gogradle/reports/test/classes/github_DOT_com.my.project.a.a1_test_DOT_go.html'))
        Document a1 = Jsoup.parse(a1Html)
        assert a1Html.contains('h1,li>a,td>a,.breadcrumbs>a')
        assert a1.select('#tests>.counter')[0].text() == '2'
        assert a1.select('#failures>.counter')[0].text() == '1'

        ['a', 'b', 'c', 'd'].each {
            assert new File(resource, ".gogradle/reports/test/packages/github_DOT_com.my.project.${it}.html").exists()
        }

        ['a1', 'a2'].each {
            assert new File(resource, ".gogradle/reports/test/classes/github_DOT_com.my.project.a.${it}_test_DOT_go.html").exists()
        }

    }

    @Override
    File getProjectRoot() {
        return resource
    }
}
