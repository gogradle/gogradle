package com.github.blindpirate.gogradle.task.go

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.task.TaskTest
import com.github.blindpirate.gogradle.util.IOUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer

import static org.mockito.ArgumentMatchers.anyList
import static org.mockito.ArgumentMatchers.isNull
import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
@WithResource('go-test-cover.zip')
class GoCoverTaskTest extends TaskTest {

    File resource

    GoCoverTask task

    @Before
    void setUp() {

        task = buildTask(GoCoverTask)

        when(project.getRootDir()).thenReturn(new File(resource, 'src/github.com/my/project'))
        when(setting.getPackagePath()).thenReturn('github.com/my/project')

        when(project.getName()).thenReturn('myProject')
        when(buildManager.go(anyList(), isNull())).thenAnswer(new Answer<Object>() {
            @Override
            Object answer(InvocationOnMock invocation) throws Throwable {
                List args = invocation.getArgument(0)
                // -html=github.com%2Fmy%2Fproject%2Fa
                String profileArg = args[2]

                if (profileArg.endsWith('a')) {
                    File srcHtml = new File(resource, "html/a.html")
                    File destHtml = new File(project.getRootDir(), '.gogradle/coverage/reports/github.com%2Fmy%2Fproject%2Fa.html')
                    IOUtils.copyFile(srcHtml, destHtml)
                } else {
                    File srcHtml = new File(resource, "html/b.html")
                    File destHtml = new File(project.getRootDir(), '.gogradle/coverage/reports/github.com%2Fmy%2Fproject%2Fb.html')
                    IOUtils.copyFile(srcHtml, destHtml)
                }
                return null
            }
        })
    }

    @Test
    void 'htmls should be generated correctly'() {
        // when
        task.doAddDefaultAction()
        task.actions[0].execute(task)
        // then
        String indexHtml = IOUtils.toString(new File(project.getRootDir(), '.gogradle/coverage/reports/index.html'))
        Document document = Jsoup.parse(indexHtml)

        assert document.select('h1').first().text() == 'myProject'

        Elements pkgs = document.select('tbody > tr')

        assert pkgs[0].child(0).id() == 'a1'
        assert pkgs[0].child(0).child(0).attr('href') == 'github.com%2Fmy%2Fproject%2Fa.html'
        assert pkgs[0].child(0).child(0).text() == 'github.com/my/project/a'

        assert pkgs[0].child(1).id() == 'b0'
        def redbar = pkgs[0].child(1).child(0)
        assert redbar.attr('width') == '20' // 20px
        assert redbar.attr('title') == '4'  // 4 lines
        def greenbar = pkgs[0].child(1).child(1)
        assert greenbar.attr('width') == '99' // 99px
        assert greenbar.attr('title') == '19'  // 19 lines

        assert pkgs[0].child(2).id() == 'c0'
        assert pkgs[0].child(2).text() == '83%'


        assert pkgs[1].child(0).id() == 'a0'
        assert pkgs[1].child(0).child(0).attr('href') == 'github.com%2Fmy%2Fproject%2Fb.html'
        assert pkgs[1].child(0).child(0).text() == 'github.com/my/project/b'

        assert pkgs[1].child(1).id() == 'b1'
        redbar = pkgs[1].child(1).child(0)
        assert redbar.attr('width') == '15' // 15px
        assert redbar.attr('title') == '3'  // 3 lines
        greenbar = pkgs[1].child(1).child(1)
        assert greenbar.attr('width') == '36' // 36px
        assert greenbar.attr('title') == '7'  // 7 lines

        assert pkgs[1].child(2).id() == 'c1'
        assert pkgs[1].child(2).text() == '70%'

        ['.gogradle/coverage/reports/github.com%2Fmy%2Fproject%2Fa.html',
         '.gogradle/coverage/reports/github.com%2Fmy%2Fproject%2Fb.html'].each {
            String html = IOUtils.toString(new File(project.getRootDir(), it))
            Document doc = Jsoup.parse(html)

            assert doc.select('#nav>a')[0].text() == 'myProject'
            assert doc.select('#nav>a')[0].attr('href') == 'index.html'
            assert doc.select('#nav>span')[0].text() == '>'
        }
    }

    @Test(expected = IllegalStateException)
    void 'exception should be thrown if io error occurs'() {
        task.extractCoverageInfo(new File('/unexistent'))
    }

}

