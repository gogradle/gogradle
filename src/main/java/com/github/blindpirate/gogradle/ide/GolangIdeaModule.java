package com.github.blindpirate.gogradle.ide;

import com.github.blindpirate.gogradle.task.GolangTaskContainer;
import com.github.blindpirate.gogradle.util.IOUtils;
import org.gradle.api.Task;
import org.gradle.api.internal.TaskInternal;
import org.gradle.plugins.ide.idea.model.Dependency;
import org.gradle.plugins.ide.idea.model.IdeaModule;

import java.util.Collections;
import java.util.Set;

public class GolangIdeaModule extends IdeaModule {
    public static final String GO_LIBRARIES_DOT_XML_CONTENT = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<project version=\"4\">\n" +
            "  <component name=\"GoLibraries\">\n" +
            "    <option name=\"urls\">\n" +
            "      <list>\n" +
            "        <option value=\"file://$PROJECT_DIR$/.gogradle/project_gopath\" />\n" +
            "        <option value=\"file://$PROJECT_DIR$/.gogradle/build_gopath\" />\n" +
            "        <option value=\"file://$PROJECT_DIR$/.gogradle/test_gopath\" />\n" +
            "      </list>\n" +
            "    </option>\n" +
            "  </component>\n" +
            "</project>";

    public static final String GO_LIBRARIES_DOT_XML_PATH = ".idea/goLibraries.xml";

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
        runTask(GolangTaskContainer.PREPARE_TASK_NAME);
        runTask(GolangTaskContainer.RESOLVE_BUILD_DEPENDENCIES_TASK_NAME);
        runTask(GolangTaskContainer.RESOLVE_TEST_DEPENDENCIES_TASK_NAME);
        runTask(GolangTaskContainer.INSTALL_BUILD_DEPENDENCIES_TASK_NAME);
        runTask(GolangTaskContainer.INSTALL_TEST_DEPENDENCIES_TASK_NAME);

        generateGoLibrariesDotXml();
        return Collections.emptySet();
    }

    private void generateGoLibrariesDotXml() {
        IOUtils.write(getProject().getRootDir(), GO_LIBRARIES_DOT_XML_PATH, GO_LIBRARIES_DOT_XML_CONTENT);
    }

    private void runTask(String taskName) {
        Task task = getProject().getTasks().getByName(taskName);
        TaskInternal.class.cast(task).execute();
    }
}
