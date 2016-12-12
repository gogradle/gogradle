package com.github.blindpirate.gogradle.core.dependency;

import com.github.blindpirate.gogradle.core.GolangPackageModule;
import com.github.blindpirate.gogradle.core.dependency.parse.NotationParser;
import com.github.blindpirate.gogradle.core.dependency.resolve.DependencyFactory;
import com.google.inject.Injector;

import java.util.Collection;

public class DependencyHelper {

    public static Injector injector;

    public static GolangDependencySet parseMany(Collection<?> notations, NotationParser parser) {
        GolangDependencySet ret = new GolangDependencySet();
        for (Object notation : notations) {
            if (parser.accept(notation)) {
                ret.add(parser.produce(notation));
            }
        }
        return ret;
    }

    public static GolangDependencySet produceDependencies(GolangPackageModule module) {
        return injector.getInstance(DependencyFactory.class).produce(module);
    }
}
