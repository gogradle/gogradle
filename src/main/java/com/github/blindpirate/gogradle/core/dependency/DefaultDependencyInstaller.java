package com.github.blindpirate.gogradle.core.dependency;

import com.github.blindpirate.gogradle.core.dependency.resolve.DependencyResolver;
import com.github.blindpirate.gogradle.util.IOUtils;
import org.gradle.api.Project;

import javax.inject.Inject;
import java.nio.file.Path;

import static com.github.blindpirate.gogradle.core.InjectionHelper.INJECTOR_INSTANCE;

public class DefaultDependencyInstaller implements DependencyInstaller {
    public static final String PROJECT_GOPATH = ".gogradle/build_gopath";

    private final Project project;

    @Inject
    public DefaultDependencyInstaller(Project project) {
        this.project = project;
    }

    @Override
    public void installDependency(ResolvedDependency dependency) {
        Path targetLocation = project.getRootDir()
                .toPath()
                .resolve(PROJECT_GOPATH)
                .resolve(dependency.getName());

        IOUtils.forceMkdir(targetLocation.toFile());

        DependencyResolver resolver = INJECTOR_INSTANCE.getInstance(dependency.getResolverClass());
        resolver.reset(dependency, targetLocation.toFile());
    }
}
