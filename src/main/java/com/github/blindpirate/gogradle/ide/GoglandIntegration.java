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

package com.github.blindpirate.gogradle.ide;

import com.github.blindpirate.gogradle.build.BuildManager;
import com.github.blindpirate.gogradle.crossplatform.GoBinaryManager;
import com.github.blindpirate.gogradle.util.IOUtils;
import org.gradle.api.Project;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class GoglandIntegration extends JetBrainsIdeIntegration {
    private static final String MISC_XML_PATH = ".idea/misc.xml";

    @Inject
    public GoglandIntegration(GoBinaryManager goBinaryManager, Project project, BuildManager buildManager) {
        super(goBinaryManager, project, buildManager);
    }

    @Override
    protected void generateModuleIml() {
        String moduleImlTemplate = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("ide/gogland/module.iml.template"));
        writeFileIntoProjectRoot(render(MODULE_IML_PATH), render(moduleImlTemplate));
    }

    @Override
    protected void generateGorootConfig() {
        String goSdkXmlTemplate = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("ide/gogland/misc.xml.template")
        );
        writeFileIntoProjectRoot(render(MISC_XML_PATH), render(goSdkXmlTemplate));
    }

    protected void generateGopathConfig() {
    }
}
