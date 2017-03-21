package com.github.blindpirate.gogradle.ide;

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
                           IdeaSdkHacker hacker) {
        super(goBinaryManager, project);
        this.hacker = hacker;
    }

    @Override
    protected String getModuleType() {
        return "GO_MODULE";
    }

    @Override
    protected String getModuleImlDir() {
        return ".idea/modules";
    }

    @Override
    protected void generateModuleIml() {
        String moduleImlTemplate = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("ide/idea_module.iml.template"));
        writeFileIntoProjectRoot(render(MODULE_IML_PATH), render(moduleImlTemplate));
        hacker.ensureSpecificSdkExist(goBinaryManager.getGoVersion(), goBinaryManager.getGoroot());
    }

    @Override
    protected void generateGoSdkDotXml() {
        // Nothing to do
    }

    public void hack() {
        IDEA_TASKS.forEach(this::skipTask);
        IdeaPlugin ideaPlugin = project.getPlugins().getPlugin(IdeaPlugin.class);
        GolangIdeaModule golangIdeaModule = new GolangIdeaModule(ideaPlugin.getModel().getModule());

        GenerateIdeaModule ideaModuleTask = (GenerateIdeaModule) project.getTasks().findByName("ideaModule");

        project.getTasks().findByName("idea").dependsOn(GolangTaskContainer.IDEA_TASK_NAME);

        ideaModuleTask.setModule(golangIdeaModule);
        ideaPlugin.getModel().setModule(golangIdeaModule);
        ideaPlugin.getModel().getProject().setModules(Arrays.asList(golangIdeaModule));
    }

    private void skipTask(String taskName) {
        project.getTasks().getByName(taskName).setEnabled(false);
    }
}
