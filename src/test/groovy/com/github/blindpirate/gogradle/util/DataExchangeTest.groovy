package com.github.blindpirate.gogradle.util

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.support.WithResource
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(GogradleRunner)
class DataExchangeTest {
    File resource

    @Test(expected = IllegalStateException)
    void 'unchecked exception should be thrown if IOException occurs'() {
        DataExchange.toYaml(new Object() {
            @SuppressFBWarnings('UMAC_UNCALLABLE_METHOD_OF_ANONYMOUS_CLASS')
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
