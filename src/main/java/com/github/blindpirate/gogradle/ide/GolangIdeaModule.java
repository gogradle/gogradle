package com.github.blindpirate.gogradle.ide;

import com.github.blindpirate.gogradle.task.GolangTaskContainer;
import org.gradle.api.Task;
import org.gradle.api.internal.TaskInternal;
import org.gradle.api.logging.Logging;
import org.gradle.plugins.ide.idea.model.Dependency;
import org.gradle.plugins.ide.idea.model.IdeaModule;

import java.util.Collections;
import java.util.Set;

public class GolangIdeaModule extends IdeaModule {
    public GolangIdeaModule(IdeaModule ideaModule) {
        super(ideaModule.getProject(), ideaModule.getIml());
        setName(ideaModule.getName());
        setSourceDirs(ideaModule.getSourceDirs());
        setGeneratedSourceDirs(ideaModule.getGeneratedSourceDirs());
        setScopes(ideaModule.getScopes());
        setDownloadJavadoc(ideaModule.isDownloadJavadoc());
        setDownloadSources(ideaModule.isDownloadSources());
        setContentRoot(ideaModule.getContentRoot());
        setTestSourceDirs(ideaModule.getTestSourceDirs());
        setExcludeDirs(ideaModule.getExcludeDirs());
        setInheritOutputDirs(ideaModule.getInheritOutputDirs());
        setOutputDir(ideaModule.getOutputDir());
        setTestOutputDir(ideaModule.getTestOutputDir());
        setPathVariables(ideaModule.getPathVariables());
        setJdkName(ideaModule.getJdkName());
        setLanguageLevel(ideaModule.getLanguageLevel());
        setTargetBytecodeVersion(ideaModule.getTargetBytecodeVersion());
        setScalaPlatform(ideaModule.getScalaPlatform());
        setPathFactory(ideaModule.getPathFactory());
        setOffline(ideaModule.isOffline());
        setSingleEntryLibraries(ideaModule.getSingleEntryLibraries());
    }

    @Override
    public Set<Dependency> resolveDependencies() {
        Logging.getLogger(getClass()).quiet("haha");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        runTask(GolangTaskContainer.PREPARE_TASK_NAME);
        runTask(GolangTaskContainer.RESOLVE_BUILD_DEPENDENCIES_TASK_NAME);
        runTask(GolangTaskContainer.RESOLVE_TEST_DEPENDENCIES_TASK_NAME);
        runTask(GolangTaskContainer.INSTALL_BUILD_DEPENDENCIES_TASK_NAME);
        runTask(GolangTaskContainer.INSTALL_TEST_DEPENDENCIES_TASK_NAME);
        return Collections.emptySet();
    }

    private void runTask(String taskName) {
        Task task = getProject().getTasks().getByName(taskName);
        TaskInternal.class.cast(task).execute();
    }
}
