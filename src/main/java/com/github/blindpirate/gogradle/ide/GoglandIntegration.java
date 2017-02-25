package com.github.blindpirate.gogradle.ide;

import com.github.blindpirate.gogradle.crossplatform.GoBinaryManager;
import com.google.inject.Inject;
import org.gradle.api.Project;

import javax.inject.Singleton;

@Singleton
@SuppressWarnings("checkstyle:linelength")
public class GoglandIntegration extends IdeIntegration {
    private static final String MODULE_IML_PATH = ".idea/${projectName}.iml";
    private static final String MODULE_IML_CONTENT = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<module type=\"${moduleType}\" version=\"4\">\n"
            + "  <component name=\"NewModuleRootManager\">\n"
            + "    <content url=\"file://$MODULE_DIR$\" />\n"
            + "    <orderEntry type=\"sourceFolder\" forTests=\"false\" />\n"
            + "    <orderEntry type=\"library\" scope=\"PROVIDED\" name=\"Go SDK\" level=\"project\" />\n"
            + "  </component>\n"
            + "</module>";

    private static final String GO_SDK_DOT_XML_PATH = ".idea/libraries/Go_SDK.xml";
    private static final String GO_SDK_DOT_XML_CONTENT = "<component name=\"libraryTable\">\n"
            + "  <library name=\"Go SDK\">\n"
            + "    <CLASSES>\n"
            + "      <root url=\"file://${goRootSrc}\" />\n"
            + "    </CLASSES>\n"
            + "    <SOURCES>\n"
            + "      <root url=\"file://${goRootSrc}\" />\n"
            + "    </SOURCES>\n"
            + "  </library>\n"
            + "</component>";

    @Inject
    public GoglandIntegration(GoBinaryManager goBinaryManager, Project project) {
        super(goBinaryManager, project);
    }

    @Override
    protected String getModuleType() {
        return "WEB_MODULE";
    }

    @Override
    protected String getModuleImlDir() {
        return ".idea";
    }

    @Override
    protected void generateModuleIml() {
        writeFileIntoProjectRoot(render(MODULE_IML_PATH), render(MODULE_IML_CONTENT));
    }

    @Override
    protected void generateGoSdkDotXml() {
        writeFileIntoProjectRoot(render(GO_SDK_DOT_XML_PATH), render(GO_SDK_DOT_XML_CONTENT));
    }
}
