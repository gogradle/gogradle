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
import com.github.blindpirate.gogradle.util.StringUtils;
import org.gradle.api.Project;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public abstract class IdeIntegration {

    private static final String GO_LIBRARIES_DOT_XML_PATH = ".idea/goLibraries.xml";

    private static final String MODULES_DOT_XML_PATH = ".idea/modules.xml";

    protected final GoBinaryManager goBinaryManager;

    protected final Project project;

    private final BuildManager buildManager;

    private final Map<String, Object> context = new HashMap<>();

    protected IdeIntegration(GoBinaryManager goBinaryManager, Project project, BuildManager buildManager) {
        this.goBinaryManager = goBinaryManager;
        this.project = project;
        this.buildManager = buildManager;
    }

    protected abstract String getModuleType();

    protected abstract String getModuleImlDir();

    protected void generateXmls() {
        generateGoLibrariesDotXml();
        generateModuleIml();
        generateGoSdkDotXml();
        generateModulesDotXml();
    }

    private void generateGoLibrariesDotXml() {
        String goLibrariesXmlTemplate = IOUtils.toString(
                IdeIntegration.class.getClassLoader().getResourceAsStream("ide/goLibraries.xml.template"));
        writeFileIntoProjectRoot(GO_LIBRARIES_DOT_XML_PATH, render(goLibrariesXmlTemplate));
    }

    protected abstract void generateModuleIml();

    protected abstract void generateGoSdkDotXml();

    private void generateModulesDotXml() {
        String modulesDotXmlTemplate = IOUtils.toString(
                IdeIntegration.class.getClassLoader().getResourceAsStream("ide/modules.xml.template"));
        String content = render(modulesDotXmlTemplate);
        writeFileIntoProjectRoot(MODULES_DOT_XML_PATH, content);
    }

    protected void writeFileIntoProjectRoot(String relativePath, String content) {
        IOUtils.write(project.getRootDir(), relativePath, content);
    }

    protected String render(String template) {
        return StringUtils.render(template, getContext());
    }

    private Map<String, Object> getContext() {
        if (context.isEmpty()) {
            loadContext();
        }
        return context;
    }

    private void loadContext() {
        Path gorootSrcPath = goBinaryManager.getGoroot().resolve("src");

        context.put("goRootSrc", StringUtils.toUnixString(gorootSrcPath));
        context.put("projectName", project.getName());
        context.put("moduleType", getModuleType());
        context.put("moduleImlDir", getModuleImlDir());
        context.put("goVersion", goBinaryManager.getGoVersion());
        if (buildManager.getGopath().endsWith(".gogradle/project_gopath")) {
            context.put("gopath", "file://$PROJECT_DIR$/.gogradle/project_gopath");
        } else {
            context.put("gopath", "file://" + buildManager.getGopath());
        }
    }
}
