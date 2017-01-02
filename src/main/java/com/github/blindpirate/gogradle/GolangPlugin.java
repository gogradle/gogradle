package com.github.blindpirate.gogradle;

import com.github.blindpirate.gogradle.core.InjectionHelper;
import com.github.blindpirate.gogradle.core.GolangConfigurationContainer;
import com.github.blindpirate.gogradle.core.dependency.GolangDependencyHandler;
import com.github.blindpirate.gogradle.core.dependency.parse.DefaultNotationParser;
import com.github.blindpirate.gogradle.core.task.BuildTask;
import com.github.blindpirate.gogradle.core.task.CleanTask;
import com.github.blindpirate.gogradle.core.task.DependenciesTask;
import com.github.blindpirate.gogradle.core.task.InstallTask;
import com.github.blindpirate.gogradle.core.task.PrepareTask;
import com.github.blindpirate.gogradle.core.task.ResolveTask;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.internal.reflect.Instantiator;

import javax.inject.Inject;

public class GolangPlugin implements Plugin<Project> {

    // prepare everything
    public static final String PREPARE_TASK_NAME = "prepare";
    // produce all dependencies by analyzing build.gradle
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

    private Injector injector;
    private GolangPluginSetting settings;
    private Project project;
    private Action<? super Task> dependencyInjectionAction = new Action<Task>() {
        @Override
        public void execute(Task task) {
            injector.injectMembers(task);
        }
    };

    // This is invoked by gradle, not our Guice.
    @Inject
    public GolangPlugin(Instantiator instantiator) {
        this.instantiator = instantiator;
    }

    private Injector initGuice() {
        return Guice.createInjector(new GogradleModule(project, instantiator));
    }

    @Override
    public void apply(Project project) {
        init(project);
        customizeProjectInternalServices(project);
        configureSettings(project);
        configureConfigurations(project);
        configureTasks(project);
        configureGlobalInjector();
        addHookToProject(project);
    }

    private void init(Project project) {
        this.project = project;
        this.injector = initGuice();
        this.settings = injector.getInstance(GolangPluginSetting.class);
    }

    private void configureGlobalInjector() {
        InjectionHelper.INJECTOR_INSTANCE = injector;
    }

    private void addHookToProject(Project project) {
        project.afterEvaluate(p -> settings.verify());
    }

    private void configureTasks(Project project) {
        TaskContainer taskContainer = project.getTasks();
        taskContainer.create(PREPARE_TASK_NAME, PrepareTask.class, dependencyInjectionAction);
        taskContainer.create(RESOLVE_TASK_NAME, ResolveTask.class, dependencyInjectionAction);
        taskContainer.create(DEPENDENCIES_TASK_NAME, DependenciesTask.class, dependencyInjectionAction);
        taskContainer.create(CLEAN_TASK_NAME, CleanTask.class, dependencyInjectionAction);
        taskContainer.create(INSTALL_TASK_NAME, InstallTask.class, dependencyInjectionAction);
        taskContainer.create(BUILD_TASK_NAME, BuildTask.class, dependencyInjectionAction);
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
