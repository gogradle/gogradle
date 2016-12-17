package com.github.blindpirate.gogradle;

import com.github.blindpirate.gogradle.core.dependency.GolangConfigurationContainer;
import com.github.blindpirate.gogradle.core.dependency.GolangDependencyHandler;
import com.github.blindpirate.gogradle.core.dependency.parse.DefaultNotationParser;
import com.github.blindpirate.gogradle.core.task.BuildTask;
import com.github.blindpirate.gogradle.core.task.CleanTask;
import com.github.blindpirate.gogradle.core.task.DependenciesTask;
import com.github.blindpirate.gogradle.core.task.InstallTask;
import com.github.blindpirate.gogradle.core.task.PrepareTask;
import com.github.blindpirate.gogradle.core.task.ResolveTask;
import com.github.blindpirate.gogradle.vcs.VcsType;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.internal.reflect.Instantiator;

import javax.inject.Inject;

public class GolangPlugin implements Plugin<Project> {

    // prepare everything
    public static final String PREPARE_TASK_NAME = "prepare";
    // resolve all dependencies by analyzing build.gradle
    public static final String RESOLVE_TASK_NAME = "resolve";
    // show dependencies tree
    public static final String DEPENDENCIES_TASK_NAME = "dependencies";

    public static final String CHECK_TASK_NAME = "check";
    public static final String BUILD_TASK_NAME = "build";
    public static final String CLEAN_TASK_NAME = "clean";
    public static final String INSTALL_TASK_NAME = "install";
    public static final String TEST_TASK_NAME = "test";
    public static final String COVERAGE_CHECK_TASK_NAME = "coverageCheck";

    public static final String BUILD_CONFIGURATION_NAME = "build";
    private static final String TEST_CONFIGURATION_NAME = "test";


    // injected by gradle
    private final Instantiator instantiator;

    private final Injector injector;
    private final GolangPluginSetting settings;

    // This is invoked by gradle, not our Guice.
    @Inject
    public GolangPlugin(Instantiator instantiator) {
        this.instantiator = instantiator;
        this.injector = initGuice();
        this.settings = injector.getInstance(GolangPluginSetting.class);
    }

    private Injector initGuice() {
        return Guice.createInjector(new GogradleModule(instantiator));
    }

    @Override
    public void apply(Project project) {
        //language=RegExp
        prepareForServices();
        customizeProjectInternalServices(project);
        configureSettings(project);
        configureConfigurations(project);

        configureTasks(project);
        addHookToProject(project);
    }

    private void addHookToProject(Project project) {
        project.afterEvaluate(new Action<Project>() {
            @Override
            public void execute(Project project) {
                settings.verify();
            }
        });
    }

    private void configureTasks(Project project) {
        TaskContainer taskContainer = project.getTasks();
        taskContainer.create(PREPARE_TASK_NAME, PrepareTask.class);
        taskContainer.create(RESOLVE_TASK_NAME, ResolveTask.class);
        taskContainer.create(DEPENDENCIES_TASK_NAME, DependenciesTask.class);
        taskContainer.create(CLEAN_TASK_NAME,CleanTask.class);
        taskContainer.create(INSTALL_TASK_NAME,InstallTask.class);
        taskContainer.create(BUILD_TASK_NAME,BuildTask.class);
    }

    private void prepareForServices() {
        VcsType.setInjector(injector);
    }

    /**
     * Here we cannot use Guice since we need GroovyObject to be mixed in
     * See {@link org.gradle.api.internal.AbstractClassGenerator}
     */
    private void customizeProjectInternalServices(Project project) {
        DefaultNotationParser parser = injector.getInstance(DefaultNotationParser.class);

        GolangConfigurationContainer configurationContainer =
                instantiator.newInstance(GolangConfigurationContainer.class,
                        instantiator);
        project.setProperty("configurationContainer", configurationContainer);

        project.setProperty("dependencyHandler",
                instantiator.newInstance(GolangDependencyHandler.class,
                        configurationContainer,
                        parser));
    }

    private void configureConfigurations(Project project) {
        ConfigurationContainer configurations = project.getConfigurations();
        configurations.create(BUILD_CONFIGURATION_NAME);
        configurations.create(TEST_CONFIGURATION_NAME);
    }

    private void configureSettings(Project project) {
        project.getExtensions().add("golang", settings);
    }
}
