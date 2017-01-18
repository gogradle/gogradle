package com.github.blindpirate.gogradle.core;

import com.google.inject.Injector;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.gradle.api.Project;

public class InjectionHelper {

    @SuppressWarnings({"checkstyle:staticvariablename", "checkstyle:visibilitymodifier"})
    @SuppressFBWarnings("MS_CANNOT_BE_FINAL")
    public static Injector INJECTOR_INSTANCE;



    public static boolean isOffline() {
        Project project = INJECTOR_INSTANCE.getInstance(Project.class);
        return project.getGradle().getStartParameter().isOffline();
    }

//    @SuppressFBWarnings("UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD")
//    public static Optional<GolangDependencySet> produceDependencies(AbstractResolvedDependency module) {
//        return INJECTOR_INSTANCE.getInstance(DependencyFactory.class).produce(module);
//    }
//
//    public static <T extends DependencyProduceStrategy> T strategy(Class<T> clazz) {
//        return INJECTOR_INSTANCE.getInstance(clazz);
//    }
}
