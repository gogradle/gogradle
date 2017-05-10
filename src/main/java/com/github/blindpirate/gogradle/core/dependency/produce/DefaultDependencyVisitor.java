package com.github.blindpirate.gogradle.core.dependency.produce;

import com.github.blindpirate.gogradle.core.GolangConfiguration;
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet;
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency;
import com.github.blindpirate.gogradle.util.logging.DebugLog;
import com.google.inject.BindingAnnotation;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.List;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Singleton
public class DefaultDependencyVisitor implements DependencyVisitor {

    private final List<ExternalDependencyFactory> externalDependencyFactories;

    private final SourceCodeDependencyFactory sourceCodeDependencyFactory;

    private final VendorDependencyFactory vendorDependencyFactory;

    @Inject
    public DefaultDependencyVisitor(@ExternalDependencyFactories
                                            List<ExternalDependencyFactory> externalDependencyFactories,
                                    SourceCodeDependencyFactory sourceCodeDependencyFactory,
                                    VendorDependencyFactory vendorDependencyFactory) {
        this.externalDependencyFactories = externalDependencyFactories;
        this.sourceCodeDependencyFactory = sourceCodeDependencyFactory;
        this.vendorDependencyFactory = vendorDependencyFactory;
    }

    @Override
    @DebugLog
    public GolangDependencySet visitExternalDependencies(ResolvedDependency dependency,
                                                         File rootDir,
                                                         String configuration) {
        for (ExternalDependencyFactory factory : externalDependencyFactories) {
            if (factory.canRecognize(rootDir)) {
                return factory.produce(rootDir, configuration);
            }
        }
        return GolangDependencySet.empty();
    }

    @Override
    @DebugLog
    public GolangDependencySet visitVendorDependencies(ResolvedDependency dependency,
                                                       File rootDir,
                                                       String configuration) {
        if (GolangConfiguration.BUILD.equals(configuration)) {
            return vendorDependencyFactory.produce(dependency, rootDir);
        } else {
            return GolangDependencySet.empty();
        }
    }

    @Override
    @DebugLog
    public GolangDependencySet visitSourceCodeDependencies(ResolvedDependency dependency,
                                                           File rootDir,
                                                           String configuration) {
        return sourceCodeDependencyFactory.produce(dependency, rootDir, configuration);
    }


    @BindingAnnotation
    @Target({FIELD, PARAMETER, METHOD})
    @Retention(RUNTIME)
    public @interface ExternalDependencyFactories {
    }
}
