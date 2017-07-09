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
import com.github.blindpirate.gogradle.task.GolangTaskContainer;
import com.github.blindpirate.gogradle.util.IOUtils;
import org.gradle.api.Project;
import org.gradle.plugins.ide.idea.GenerateIdeaModule;
import org.gradle.plugins.ide.idea.IdeaPlugin;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.List;

import static java.util.Collections.singletonList;

@SuppressWarnings("checkstyle:linelength")
@Singleton
public class IdeaIntegration extends IdeIntegration {
    private static final List<String> IDEA_TASKS = Arrays.asList("cleanIdea", "cleanIdeaProject",
            "cleanIdeaModule", "cleanIdeaWorkspace", "ideaProject", "ideaModule", "ideaWorkspace");


    private static final String MODULE_IML_PATH = ".idea/modules/${projectName}.iml";

    private final IdeaSdkHacker hacker;

    @Inject
    public IdeaIntegration(GoBinaryManager goBinaryManager,
                           Project project,
                           BuildManager buildManager,
                           IdeaSdkHacker hacker) {
        super(goBinaryManager, project, buildManager);
        this.hacker = hacker;
    }

    @Override
    protected String getModuleImlDir() {
        return ".idea/modules";
    }

    @Override
    protected void generateGorootConfig() {
        hacker.ensureSpecificSdkExist(goBinaryManager.getGoVersion(), goBinaryManager.getGoroot());
    }

    @Override
    protected void generateModuleIml() {
        String moduleImlTemplate = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("ide/idea/module.iml.template"));
        writeFileIntoProjectRoot(render(MODULE_IML_PATH), render(moduleImlTemplate));
    }

    public void hack() {
        IDEA_TASKS.forEach(this::skipTask);
        IdeaPlugin ideaPlugin = project.getPlugins().getPlugin(IdeaPlugin.class);
        GolangIdeaModule golangIdeaModule = new GolangIdeaModule(ideaPlugin.getModel().getModule());

        GenerateIdeaModule ideaModuleTask = (GenerateIdeaModule) project.getTasks().findByName("ideaModule");

        project.getTasks().findByName("idea").dependsOn(GolangTaskContainer.IDEA_TASK_NAME);

        ideaModuleTask.setModule(golangIdeaModule);
        ideaPlugin.getModel().setModule(golangIdeaModule);
        ideaPlugin.getModel().getProject().setModules(singletonList(golangIdeaModule));
    }

    private void skipTask(String taskName) {
        project.getTasks().getByName(taskName).setEnabled(false);
    }
}
