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

package com.github.blindpirate.gogradle.ide

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.crossplatform.Os
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.util.IOUtils
import com.github.blindpirate.gogradle.util.ReflectionUtils
import com.github.blindpirate.gogradle.util.StringUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

import static com.github.blindpirate.gogradle.util.ExceptionHandler.UncheckedException
import static org.apache.commons.lang3.StringUtils.countMatches

@RunWith(GogradleRunner)
@WithResource('')
class IdeaSdkHackerTest {
    File resource

    IdeaSdkHacker hacker = new IdeaSdkHacker()

    @Before
    void setUp() {
        System.setProperty("user.home", resource.absolutePath)
    }

    String xmlWithGoSdk = '''
<application>
  <component name="ProjectJdkTable">
    <jdk version="2">
      <name value="IntelliJ IDEA IU-162.1812.17" />
      <type value="IDEA JDK" />
      <version value="java version &quot;1.8.0_74&quot;" />
      <homePath value="$APPLICATION_HOME_DIR$" />
      <roots>
        <annotationsPath>
          <root type="composite">
            <root type="simple" url="jar://$APPLICATION_HOME_DIR$/lib/jdkAnnotations.jar!/" />
          </root>
        </annotationsPath>
        <classPath>
          <root type="composite">
            <root type="simple" url="jar://$APPLICATION_HOME_DIR$/plugins/Spring/lib/spring-web.jar!/" />
          </root>
        </classPath>
        <javadocPath>
          <root type="composite" />
        </javadocPath>
        <sourcePath>
          <root type="composite">
            <root type="simple" url="jar://$APPLICATION_HOME_DIR$/lib/src/trove4j_src.jar!/" />
          </root>
        </sourcePath>
      </roots>
      <additional sdk="1.8 ">
        <option name="mySandboxHome" value="$USER_HOME$/Library/Caches/IntelliJIdea2016.2/plugins-sandbox" />
      </additional>
    </jdk>
    <jdk version="2">
      <name value="Go 1.7.1" />
      <type value="Go SDK" />
      <version value="1.7.1" />
      <homePath value="/usr/local/Cellar/go/1.7.1/libexec" />
      <roots>
        <annotationsPath>
          <root type="composite" />
        </annotationsPath>
        <classPath>
          <root type="composite">
            <root type="simple" url="file:///usr/local/Cellar/go/1.7.1/libexec/src" />
          </root>
        </classPath>
        <javadocPath>
          <root type="composite" />
        </javadocPath>
        <sourcePath>
          <root type="composite">
            <root type="simple" url="file:///usr/local/Cellar/go/1.7.1/libexec/src" />
          </root>
        </sourcePath>
      </roots>
      <additional />
    </jdk>
  </component>
</application>
'''
    String xmlWithoutSpecificGoSdk = '''
<application>
  <component name="ProjectJdkTable"> 
  <jdk version="2">
      <name value="Go 1.7.4" />
      <type value="Go SDK" />
      <version value="1.7.4" />
      <homePath value="/usr/local/Cellar/go/1.7.4/libexec" />
      <roots>
        <annotationsPath>
          <root type="composite" />
        </annotationsPath>
        <classPath>
          <root type="composite">
            <root type="simple" url="file:///usr/local/Cellar/go/1.7.4/libexec/src" />
          </root>
        </classPath>
        <javadocPath>
          <root type="composite" />
        </javadocPath>
        <sourcePath>
          <root type="composite">
            <root type="simple" url="file:///usr/local/Cellar/go/1.7.4/libexec/src" />
          </root>
        </sourcePath>
      </roots>
      <additional />
    </jdk>
  </component>
</application>
'''

    @Test
    void 'sdk should be added if not exist'() {
        writeInto('IntelliJIdea', '2016.1', xmlWithoutSpecificGoSdk)
        writeInto('IntelliJIdea', '2016.3', xmlWithoutSpecificGoSdk)
        writeInto('IdeaIC', '2016.1', xmlWithoutSpecificGoSdk)
        writeInto('IdeaIC', '2016.3', xmlWithoutSpecificGoSdk)
        hacker.ensureSpecificSdkExist('1.7.1', resource.toPath())

        String location = "url=\"file://${StringUtils.toUnixString(new File(resource, 'src'))}\""

        assert countMatches(getFileContent('IntelliJIdea', '2016.1'), location) == 2
        assert countMatches(getFileContent('IntelliJIdea', '2016.3'), location) == 2
        assert countMatches(getFileContent('IdeaIC', '2016.1'), location) == 2
        assert countMatches(getFileContent('IdeaIC', '2016.3'), location) == 2

        ['Go 1.7.1', 'Go SDK'].each {
            assert getFileContent('IntelliJIdea', '2016.1').contains(it)
            assert getFileContent('IntelliJIdea', '2016.3').contains(it)
            assert getFileContent('IdeaIC', '2016.1').contains(it)
            assert getFileContent('IdeaIC', '2016.3').contains(it)
        }
    }

    @Test
    void 'file should not be changed if specific sdk exists'() {

        writeInto('IntelliJIdea', '2016.1', xmlWithGoSdk)
        writeInto('IntelliJIdea', '2016.3', xmlWithGoSdk)
        writeInto('IdeaIC', '2016.1', xmlWithGoSdk)
        writeInto('IdeaIC', '2016.3', xmlWithGoSdk)

        List<String> fileContents = loadFileContents()
        hacker.ensureSpecificSdkExist('1.7.1', resource.toPath())
        assert loadFileContents() == fileContents
    }

    @Test(expected = UncheckedException)
    void 'exceptions should be thrown if xml is corrupted'() {
        writeInto('IdeaIC', '2016.1', '<badxml><')
        hacker.ensureSpecificSdkExist('1.7.1', resource.toPath())
    }

    List loadFileContents() {
        return [getFileContent("IntelliJIdea", '2016.1'),
                getFileContent("IntelliJIdea", '2016.3'),
                getFileContent("IdeaIC", '2016.1'),
                getFileContent("IdeaIC", '2016.3')]
    }

    void writeInto(String product, String version, String xml) {
        IOUtils.write(new File(getLocation(product, version)), xml)
    }

    String getFileContent(String product, String version) {
        return IOUtils.toString(new File(getLocation(product, version)))
    }

    String getLocation(String product, String version) {
        String location
        if (Os.getHostOs() == Os.DARWIN) {
            location = ReflectionUtils.getStaticField(IdeaSdkHacker, "SETTING_LOCATION_ON_MAC")
        } else {
            location = ReflectionUtils.getStaticField(IdeaSdkHacker, "SETTING_LOCATION_ON_OTHER_OS")
        }
        return StringUtils.render(location, [userHome: System.getProperty('user.home'),
                                             product : product,
                                             version : version])
    }
}
