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

package com.github.blindpirate.gogradle

import com.github.blindpirate.gogradle.crossplatform.Os
import com.github.blindpirate.gogradle.support.*
import com.github.blindpirate.gogradle.util.DataExchange
import com.github.blindpirate.gogradle.util.IOUtils
import org.gradle.tooling.BuildController
import org.gradle.tooling.model.idea.IdeaProject
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

import static com.github.blindpirate.gogradle.util.StringUtils.toUnixString

@WithResource('')
@RunWith(GogradleRunner)
@WithIsolatedUserhome
@WithMockGo
@WithGitRepo(repoName = 'a', fileName = 'a.go')
class IdeIntegrationTest extends IntegrationTestSupport implements Serializable {
    static final long serialVersionUID = 1L

    File jdkDotTableDotXmlFile

    @Override
    File getProjectRoot() {
        return resource
    }

    @Before
    void setUp() {
        String buildDotGradle = """
${buildDotGradleBase}
golang {
    packagePath='github.com/my/awesome'
    goRoot='${toUnixString(resource)}'
}

dependencies {
    golang {
        build name:'localhost/a', url:'http://localhost:8080/a'
    }
}
System.setProperty('user.home','${toUnixString(userhome)}')
"""
        writeBuildAndSettingsDotGradle(buildDotGradle, 'rootProject.name="awesome"')

        if (Os.getHostOs() == Os.DARWIN) {
            jdkDotTableDotXmlFile = new File("${userhome}/Library/Preferences/IntelliJIdea2017.1/options/jdk.table.xml")
        } else {
            jdkDotTableDotXmlFile = new File("${userhome}/.IntelliJIdea2017.1/config/options/jdk.table.xml")
        }

        IOUtils.write(jdkDotTableDotXmlFile, jdkDotTableDotXml)
    }

    @Test
    void 'getting IdeaProject should succeed'() {
        IdeaProject project = buildAction { BuildController controller ->
            controller.getModel(IdeaProject)
        }

        assert project.name == 'awesome'
        assert new File(resource, 'vendor/localhost/a/a.go').exists()
    }

    @Test
    void 'idea task should succeed'() {
        newBuild {
            it.forTasks('goIdea')
        }
        assert new File(resource, 'vendor/localhost/a/a.go').exists()
        verifyIdeaXmlsCorrect()
    }


    @Test
    void 'idea task with idea plugin should succeed'() {
        new File(resource, 'build.gradle').append('apply plugin:"idea"')
        newBuild {
            it.forTasks('goIdea')
        }
        assert new File(resource, 'vendor/localhost/a/a.go').exists()
        verifyIdeaXmlsCorrect()
    }

    @Test
    void 'vscode task should succeed'() {
        newBuild {
            it.forTasks('vscode')
        }
        assert new File(resource, 'vendor/localhost/a/a.go').exists()
        String json = new File(resource, '.vscode/settings.json').text
        assert DataExchange.parseJson(json, Map)['go.gopath'] == toUnixString(new File(resource, '.gogradle/project_gopath'))
    }

    @Test
    void 'other JetBrains task should succeed'() {
        newBuild {
            it.forTasks('gogland')
        }
        assert new File(resource, 'vendor/localhost/a/a.go').exists()
        verifyJetBrainsXml()
    }

    void verifyIdeaXmlsCorrect() {
        assert new File(resource, '.idea/goLibraries.xml').text.trim() == """\
<?xml version="1.0" encoding="UTF-8"?>
<project version="4">"
  <component name="GoLibraries">
    <option name="urls">
      <list>
        <option value="file://\$PROJECT_DIR\$/.gogradle/project_gopath" />
      </list>
    </option>
  </component>
</project>"""
        assert new File(resource, '.idea/modules/awesome.iml').text.trim() == """\
<?xml version="1.0" encoding="UTF-8"?>
<module external.linked.project.id="awesome" external.linked.project.path="\$MODULE_DIR\$/../.." external.root.project.path="\$MODULE_DIR\$/../.." external.system.id="GRADLE" type="GO_MODULE" version="4">
  <component name="NewModuleRootManager">
    <content url="file://\$MODULE_DIR\$/../.." />
    <orderEntry type="sourceFolder" forTests="false" />
    <orderEntry type="jdk" jdkName="Go 1.7.1" jdkType="Go SDK" />
  </component>
</module>"""
        assert new File(resource, '.idea/modules.xml').text.trim() == """\
<?xml version="1.0" encoding="UTF-8"?>
<project version="4">
  <component name="ProjectModuleManager">
    <modules>
      <module fileurl="file://\$PROJECT_DIR\$/.idea/modules/awesome.iml" filepath="\$PROJECT_DIR\$/.idea/modules/awesome.iml" />
    </modules>
  </component>
</project>"""
        assert jdkDotTableDotXmlFile.text.contains('<name value="Go 1.7.1"/>')
    }

    void verifyJetBrainsXml() {
        assert new File(resource, '.idea/goLibraries.xml').text.trim() == """\
<?xml version="1.0" encoding="UTF-8"?>
<project version="4">"
  <component name="GoLibraries">
    <option name="urls">
      <list>
        <option value="file://\$PROJECT_DIR\$/.gogradle/project_gopath" />
      </list>
    </option>
  </component>
</project>"""
        assert new File(resource, '.idea//awesome.iml').text.trim() == """\
<?xml version="1.0" encoding="UTF-8"?>
<module type="WEB_MODULE" version="4">
  <component name="NewModuleRootManager">
    <content url="file://\$MODULE_DIR\$" />
    <orderEntry type="sourceFolder" forTests="false" />
    <orderEntry type="library" scope="PROVIDED" name="Go SDK" level="project" />
  </component>
</module>"""
        assert new File(resource, '.idea/libraries/Go_SDK.xml').text == """\
<component name="libraryTable">
  <library name="Go SDK">
    <CLASSES>
      <root url="file://${toUnixString(resource)}/src" />
    </CLASSES>
    <SOURCES>
      <root url="file://${toUnixString(resource)}/src" />
    </SOURCES>
  </library>
</component>"""
        assert new File(resource, '.idea/modules.xml').text.trim() == """\
<?xml version="1.0" encoding="UTF-8"?>
<project version="4">
  <component name="ProjectModuleManager">
    <modules>
      <module fileurl="file://\$PROJECT_DIR\$/.idea/awesome.iml" filepath="\$PROJECT_DIR\$/.idea/awesome.iml" />
    </modules>
  </component>
</project>"""
    }
    String jdkDotTableDotXml = '''\
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<application>
  <component name="ProjectJdkTable">
    <jdk version="2">
      <name value="1.8_121"/>
      <type value="JavaSDK"/>
      <version value="java version &quot;1.8.0_121&quot;"/>
      <homePath value="/Library/Java/JavaVirtualMachines/jdk1.8.0_121.jdk/Contents/Home"/>
      <roots>
        <annotationsPath>
          <root type="composite">
            <root type="simple" url="jar://$APPLICATION_HOME_DIR$/lib/jdkAnnotations.jar!/"/>
          </root>
        </annotationsPath>
        <classPath>
          <root type="composite">
            <root type="simple" url="jar:///Library/Java/JavaVirtualMachines/jdk1.8.0_121.jdk/Contents/Home/jre/lib/charsets.jar!/"/>
          </root>
        </classPath>
        <javadocPath>
          <root type="composite"/>
        </javadocPath>
        <sourcePath>
          <root type="composite">
            <root type="simple" url="jar:///Library/Java/JavaVirtualMachines/jdk1.8.0_121.jdk/Contents/Home/src.zip!/"/>
            <root type="simple" url="jar:///Library/Java/JavaVirtualMachines/jdk1.8.0_121.jdk/Contents/Home/javafx-src.zip!/"/>
          </root>
        </sourcePath>
      </roots>
      <additional/>
    </jdk>
</component>
</application>
'''
}
