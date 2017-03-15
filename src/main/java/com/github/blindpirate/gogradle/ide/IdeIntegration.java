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

    private static final String MODULES_DOT_XML_PATH = ".idea/modules.xml";

    protected final GoBinaryManager goBinaryManager;

    protected final Project project;

    private final Map<String, Object> context = new HashMap<>();

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
        writeFileIntoProjectRoot(GO_LIBRARIES_DOT_XML_PATH,
                IOUtils.toString(IdeIntegration.class.getClassLoader().getResourceAsStream("ide/goLibraries.xml")));
    }

    protected abstract void generateModuleIml();

    protected abstract void generateGoSdkDotXml();

    private void generateModulesDotXml() {
        String modulesDotXmlTemplate
                = IOUtils.toString(IdeIntegration.class.getClassLoader().getResourceAsStream("ide/modules.xml.template"));
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

        context.put("goRootSrc", gorootSrcPath.toString());
        context.put("projectName", project.getName());
        context.put("moduleType", getModuleType());
        context.put("moduleImlDir", getModuleImlDir());
        context.put("goVersion", goBinaryManager.getGoVersion());
    }
}
