package com.github.blindpirate.gogradle.ide;

import com.github.blindpirate.gogradle.crossplatform.GoBinaryManager;
import com.github.blindpirate.gogradle.util.IOUtils;
import com.github.blindpirate.gogradle.util.StringUtils;
import org.gradle.api.Project;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public abstract class IdeIntegration {

    private static final String GO_LIBRARIES_DOT_XML_PATH = ".idea/goLibraries.xml";
    private static final String GO_LIBRARIES_DOT_XML_CONTENT = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<project version=\"4\">\n"
            + "  <component name=\"GoLibraries\">\n"
            + "    <option name=\"urls\">\n"
            + "      <list>\n"
            + "        <option value=\"file://$PROJECT_DIR$/.gogradle/project_gopath\" />\n"
            + "        <option value=\"file://$PROJECT_DIR$/.gogradle/build_gopath\" />\n"
            + "        <option value=\"file://$PROJECT_DIR$/.gogradle/test_gopath\" />\n"
            + "      </list>\n"
            + "    </option>\n"
            + "  </component>\n"
            + "</project>";

    private static final String MODULES_DOT_XML_PATH = ".idea/modules.xml";
    private static final String MODULES_DOT_XML_CONTENT = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<project version=\"4\">\n"
            + "  <component name=\"ProjectModuleManager\">\n"
            + "    <modules>\n"
            + "      <module fileurl=\"file://$PROJECT_DIR$/${moduleImlDir}/${projectName}.iml\" "
            + "filepath=\"$PROJECT_DIR$/${moduleImlDir}/${projectName}.iml\" />\n"
            + "    </modules>\n"
            + "  </component>\n"
            + "</project>\n";

    protected final GoBinaryManager goBinaryManager;

    protected final Project project;

    private final Map<String, String> context = new HashMap<>();

    protected IdeIntegration(GoBinaryManager goBinaryManager, Project project) {
        this.goBinaryManager = goBinaryManager;
        this.project = project;
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
        writeFileIntoProjectRoot(GO_LIBRARIES_DOT_XML_PATH, GO_LIBRARIES_DOT_XML_CONTENT);
    }

    protected abstract void generateModuleIml();

    protected abstract void generateGoSdkDotXml();

    private void generateModulesDotXml() {
        String content = render(MODULES_DOT_XML_CONTENT);
        writeFileIntoProjectRoot(MODULES_DOT_XML_PATH, content);
    }

    protected void writeFileIntoProjectRoot(String relativePath, String content) {
        IOUtils.write(project.getRootDir(), relativePath, content);
    }

    protected String render(String template) {
        return StringUtils.render(template, getContext());
    }

    private Map<String, String> getContext() {
        if (context.isEmpty()) {
            loadContext();
        }
        return context;
    }

    private void loadContext() {
        Path gorootSrcPath = goBinaryManager.getGoroot().resolve("src");

        context.put("goRootSrc", gorootSrcPath.toString());
        context.put("projectName", project.getName());
        context.put("moduleType", getModuleType());
        context.put("moduleImlDir", getModuleImlDir());
        context.put("goVersion", goBinaryManager.getGoVersion());
    }
}
