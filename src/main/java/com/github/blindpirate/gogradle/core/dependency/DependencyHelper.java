package com.github.blindpirate.gogradle.core.dependency;

import com.github.blindpirate.gogradle.core.GolangPackageModule;
import com.github.blindpirate.gogradle.core.dependency.parse.NotationParser;
import com.github.blindpirate.gogradle.core.dependency.resolve.DependencyFactory;
import com.google.inject.Injector;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.Collection;

public class DependencyHelper {

    @SuppressWarnings({"checkstyle:staticvariablename", "checkstyle:visibilitymodifier"})
    @SuppressFBWarnings("MS_SHOULD_BE_FINAL")
    public static Injector INJECTOR_INSTANCE;

    public static GolangDependencySet parseMany(Collection<?> notations, NotationParser parser) {
        GolangDependencySet ret = new GolangDependencySet();
        for (Object notation : notations) {
            if (parser.accept(notation)) {
                ret.add(parser.produce(notation));
            }
        }
        return ret;
    }

    @SuppressFBWarnings("UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD")
    public static GolangDependencySet produceDependencies(GolangPackageModule module) {
        return INJECTOR_INSTANCE.getInstance(DependencyFactory.class).produce(module);
    }
}
