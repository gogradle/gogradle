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
import static org.mockito.ArgumentMatchers.anyMap
import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
@WithResource('go-test-cover')
class GoCoverTest extends TaskTest {

    File resource

    GoCover task

    @Before
    void setUp() {

        task = buildTask(GoCover)

        when(project.getProjectDir()).thenReturn(resource)
        when(setting.getPackagePath()).thenReturn('github.com/my/project')

        when(golangTaskContainer.get(GoTest).isCoverageProfileGenerated()).thenReturn(true)

        when(project.getName()).thenReturn(resource.getName())
        when(buildManager.go(anyList(), anyMap())).thenAnswer(new Answer<Object>() {
            @Override
            Object answer(InvocationOnMock invocation) throws Throwable {
                List args = invocation.getArgument(0)
                // -html=github.com%2Fmy%2Fproject%2Fa
                String profileArg = args[2]

                if (profileArg.endsWith('a.out')) {
                    File srcHtml = new File(resource, "a.html")
                    File destHtml = new File(project.getProjectDir(), '.gogradle/reports/coverage/github.com%2Fmy%2Fproject%2Fa.html')
                    IOUtils.copyFile(srcHtml, destHtml)
                } else if (profileArg.endsWith('b.out')) {
                    File srcHtml = new File(resource, "b.html")
                    File destHtml = new File(project.getProjectDir(), '.gogradle/reports/coverage/github.com%2Fmy%2Fproject%2Fb.html')
                    IOUtils.copyFile(srcHtml, destHtml)
                }
                return null
            }
        })
    }

    @Test
    void 'htmls should be generated correctly'() {
        // when
        task.coverage()
        // then
        examineCoverageHtmls(project.getProjectDir())
        assert task.inputCoverageDirectory == new File(resource, '.gogradle/reports/coverage/profiles')
        assert task.outputHtmls as Set == [
                new File(resource,'.gogradle/reports/coverage/index.html'),
                new File(resource,'.gogradle/reports/coverage/github.com%2Fmy%2Fproject%2Fa.html'),
                new File(resource,'.gogradle/reports/coverage/github.com%2Fmy%2Fproject%2Fb.html')
        ] as Set
    }

    static void examineCoverageHtmls(File projectRoot) {
        String indexHtml = new File(projectRoot, '.gogradle/reports/coverage/index.html').text
        Document document = Jsoup.parse(indexHtml)

        assert document.select('h1').first().text() == projectRoot.getName()

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

        ['.gogradle/reports/coverage/github.com%2Fmy%2Fproject%2Fa.html',
         '.gogradle/reports/coverage/github.com%2Fmy%2Fproject%2Fb.html'].each {
            String html = IOUtils.toString(new File(projectRoot, it))
            Document doc = Jsoup.parse(html)

            assert doc.select('#nav>a')[0].text() == projectRoot.getName()
            assert doc.select('#nav>a')[0].attr('href') == 'index.html'
            assert doc.select('#nav>span')[0].text() == '>'
        }

        // static resources
        assert new File(projectRoot, '.gogradle/reports/coverage/static').listFiles().size() > 4
    }

    @Test
    void 'coverage task should be skipped if no coverfile generated'() {
        // given
        when(golangTaskContainer.get(GoTest).isCoverageProfileGenerated()).thenReturn(false)
        // when
        task.coverage()
        // then
        // no htmls generated
        assert !new File(resource, '.gogradle/reports/coverage').list().any { it.endsWith('html') }
    }

    @Test
    @WithResource('')
    void 'coverage task should not throw exception when empty coverage html is generated'() {
        // given
        IOUtils.write(resource, '.gogradle/reports/coverage/profiles/github.com%252Fmy%252Fproject.out', '')
        IOUtils.write(resource, '.gogradle/reports/coverage/github.com%252Fmy%252Fproject.html', '''
<!DOCTYPE html>
<html><head><meta http-equiv="Content-Type" content="text/html; charset=utf-8"></head>
<body>
<div id="topbar">
    <div id="nav">
        <select id="files"></select>
    </div>
</div>
<div id="content">
</div>
</body>
</html>
''')
        // when
        task.coverage()
    }

    @Test(expected = UncheckedIOException)
    void 'exception should be thrown if io error occurs'() {
        task.extractCoverageInfo(new File('/unexistent'))
    }

}

