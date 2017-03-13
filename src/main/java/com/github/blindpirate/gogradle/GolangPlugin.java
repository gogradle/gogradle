package com.github.blindpirate.gogradle;

import com.github.blindpirate.gogradle.build.Configuration;
import com.github.blindpirate.gogradle.core.GolangConfigurationContainer;
import com.github.blindpirate.gogradle.core.dependency.GolangDependencyHandler;
import com.github.blindpirate.gogradle.core.dependency.parse.DefaultNotationParser;
import com.github.blindpirate.gogradle.ide.IdeaIntegration;
import com.github.blindpirate.gogradle.task.go.Go;
import com.github.blindpirate.gogradle.task.GolangTaskContainer;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.internal.reflect.Instantiator;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.stream.Stream;

import static com.github.blindpirate.gogradle.task.GolangTaskContainer.BUILD_TASK_NAME;
import static com.github.blindpirate.gogradle.task.GolangTaskContainer.COVERAGE_TASK_NAME;
import static com.github.blindpirate.gogradle.task.GolangTaskContainer.GOFMT_TASK_NAME;
import static com.github.blindpirate.gogradle.task.GolangTaskContainer.GOLINT_TAK_NAME;
import static com.github.blindpirate.gogradle.task.GolangTaskContainer.GOVET_TASK_NAME;
import static com.github.blindpirate.gogradle.task.GolangTaskContainer.TASKS;
import static com.github.blindpirate.gogradle.task.GolangTaskContainer.TEST_TASK_NAME;


public class GolangPlugin implements Plugin<Project> {

    // injected by gradle
    private final Instantiator instantiator;

    private Injector injector;
    private GolangPluginSetting settings;
    private GolangTaskContainer golangTaskContainer;
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
        hackIdeaPlugin();
    }

    private void hackIdeaPlugin() {
        project.getPlugins().withId("idea", plugin -> {
            injector.getInstance(IdeaIntegration.class).hack();
        });
    }

    private void init(Project project) {
        this.project = project;
        this.injector = initGuice();
        this.settings = injector.getInstance(GolangPluginSetting.class);
        this.golangTaskContainer = injector.getInstance(GolangTaskContainer.class);
    }

    private void configureGlobalInjector() {
        GogradleGlobal.INSTANCE.setInjector(injector);
    }

    private void configureTasks(Project project) {
        TaskContainer taskContainer = project.getTasks();
        TASKS.entrySet().forEach(entry -> {
            Task task = taskContainer.create(entry.getKey(), entry.getValue(), dependencyInjectionAction);
            golangTaskContainer.put((Class) entry.getValue(), task);
        });

        Arrays.asList(BUILD_TASK_NAME,
                TEST_TASK_NAME,
                COVERAGE_TASK_NAME,
                GOFMT_TASK_NAME,
                GOVET_TASK_NAME,
                GOLINT_TAK_NAME)
                .forEach(task -> Go.class.cast(taskContainer.getByName(task)).addDefaultActionIfNoCustomActions());
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
                        parser,
                        project));

        overwriteRepositoryHandler(project);
    }

    private void overwriteRepositoryHandler(Project project) {

        GitRepositoryHandler repositoryHandler = injector.getInstance(GitRepositoryHandler.class);

        ExtensionAware.class.cast(project.getRepositories()).getExtensions().add("git", repositoryHandler);

    }

    private void configureConfigurations(Project project) {
        ConfigurationContainer configurations = project.getConfigurations();
        Stream.of(Configuration.values()).map(Configuration::getName).forEach(configurations::create);
    }

    private void configureSettings(Project project) {
        project.getExtensions().add("golang", settings);
    }
}
