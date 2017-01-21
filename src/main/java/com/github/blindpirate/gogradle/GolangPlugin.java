package com.github.blindpirate.gogradle;

import com.github.blindpirate.gogradle.build.Configuration;
import com.github.blindpirate.gogradle.core.GolangConfigurationContainer;
import com.github.blindpirate.gogradle.core.dependency.GolangDependencyHandler;
import com.github.blindpirate.gogradle.core.dependency.parse.DefaultNotationParser;
import com.github.blindpirate.gogradle.task.GolangTaskContainer;
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

import java.util.stream.Stream;

import static com.github.blindpirate.gogradle.task.GolangTaskContainer.TASKS;


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
        addHookToProject(project);
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

    private void addHookToProject(Project project) {
        project.afterEvaluate(p -> settings.verify());
    }

    private void configureTasks(Project project) {
        TaskContainer taskContainer = project.getTasks();
        TASKS.entrySet().forEach(entry -> {
            Task task = taskContainer.create(entry.getKey(), entry.getValue(), dependencyInjectionAction);
            golangTaskContainer.put((Class) entry.getValue(), task);
        });
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
        Stream.of(Configuration.values()).map(Configuration::getName).forEach(configurations::create);
    }

    private void configureSettings(Project project) {
        project.getExtensions().add("golang", settings);
    }
}
