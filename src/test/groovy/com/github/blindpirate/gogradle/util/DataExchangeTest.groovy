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

package com.github.blindpirate.gogradle.util

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.support.WithResource
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(GogradleRunner)
@SuppressFBWarnings("UMAC_UNCALLABLE_METHOD_OF_ANONYMOUS_CLASS")
class DataExchangeTest {
    File resource

    @Test(expected = UncheckedIOException)
    void 'unchecked exception should be thrown if IOException occurs'() {
        DataExchange.toYaml(new Object() {
            int getId() {
                throw new IOException()
            }
        })
    }

    @Test
    @WithResource('')
    void 'reading xml value should succeed'() {
        // given
        IOUtils.write(resource, 'module.xml', '''
<?xml version="1.0" encoding="UTF-8"?>
<module external.linked.project.id="gogits_gogs" external.linked.project.path="$MODULE_DIR$/../.." external.root.project.path="$MODULE_DIR$/../.." external.system.id="GRADLE" external.system.module.group="" external.system.module.version="unspecified" type="GO_MODULE" version="4">
  <component name="NewModuleRootManager" inherit-compiler-output="true">
    <content url="file://$MODULE_DIR$/../..">
      <excludeFolder url="file://$MODULE_DIR$/../../.gradle" />
      <excludeFolder url="file://$MODULE_DIR$/../../build" />
    </content>
    <orderEntry type="sourceFolder" forTests="false" />
  </component>
</module>
''')
        // when
        Map module = DataExchange.parseXml(new File(resource, 'module.xml'), Map)
        // then
        assert module.type == 'GO_MODULE'
    }
}
