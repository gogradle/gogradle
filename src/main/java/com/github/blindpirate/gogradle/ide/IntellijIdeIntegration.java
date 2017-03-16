package com.github.blindpirate.gogradle.ide;

import com.github.blindpirate.gogradle.crossplatform.GoBinaryManager;
import com.github.blindpirate.gogradle.util.IOUtils;
import com.google.inject.Inject;
import org.gradle.api.Project;

import javax.inject.Singleton;

/**
 * For PHPStorm/PyCharm/WebStorm/Gogland/RubyMine/CLion
 */
@Singleton
@SuppressWarnings("checkstyle:linelength")
public class IntellijIdeIntegration extends IdeIntegration {
    private static final String MODULE_IML_PATH = ".idea/${projectName}.iml";

    private static final String GO_SDK_DOT_XML_PATH = ".idea/libraries/Go_SDK.xml";

    @Inject
    public IntellijIdeIntegration(GoBinaryManager goBinaryManager, Project project) {
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
        String moduleImlTemplate = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("ide/intellij_module.iml.template"));
        writeFileIntoProjectRoot(render(MODULE_IML_PATH), render(moduleImlTemplate));
    }

    @Override
    protected void generateGoSdkDotXml() {
        String goSdkXmlTemplate = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("ide/Go_SDK.xml.template")
        );
        writeFileIntoProjectRoot(render(GO_SDK_DOT_XML_PATH), render(goSdkXmlTemplate));
    }
}
