package com.github.blindpirate.gogradle.core.dependency.resolve;

import com.github.blindpirate.gogradle.core.GolangPackageModule;
import com.github.blindpirate.gogradle.core.dependency.produce.DependencyProduceStrategy;
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet;
import com.github.blindpirate.gogradle.util.FactoryUtil;
import com.google.common.base.Optional;
import com.google.inject.BindingAnnotation;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.List;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Singleton
public class DefaultDependencyFactory implements DependencyFactory, ModuleDependencyVistor {

    private final List<? extends DependencyFactory> externalDependencyFactories;

    private final SourceCodeDependencyFactory sourceCodeDependencyFactory;

    private final VendorDependencyFactory vendorDependencyFactory;

    @Inject
    public DefaultDependencyFactory(@ExternalDependencyFactories
                                            List<? extends DependencyFactory> externalDependencyFactories,
                                    SourceCodeDependencyFactory sourceCodeDependencyFactory,
                                    VendorDependencyFactory vendorDependencyFactory) {
        this.externalDependencyFactories = externalDependencyFactories;
        this.sourceCodeDependencyFactory = sourceCodeDependencyFactory;
        this.vendorDependencyFactory = vendorDependencyFactory;
    }

    @Override
    public Optional<GolangDependencySet> visitExternalDependencies(GolangPackageModule module) {
        return FactoryUtil.produce(externalDependencyFactories, module);
    }

    @Override
    public Optional<GolangDependencySet> visitVendorDependencies(GolangPackageModule module) {
        if (vendorDependencyFactory.accept(module)) {
            return Optional.of(vendorDependencyFactory.produce(module));
        } else {
            return Optional.absent();
        }
    }

    @Override
    public GolangDependencySet visitSourceCodeDependencies(GolangPackageModule module) {
        return sourceCodeDependencyFactory.produce(module);
    }


    @Override
    public GolangDependencySet produce(GolangPackageModule module) {
        DependencyProduceStrategy strategy = module.getProduceStrategy();
        return strategy.produce(module, this);
    }

    @Override
    public boolean accept(GolangPackageModule module) {
        return true;
    }

    @BindingAnnotation
    @Target({FIELD, PARAMETER, METHOD})
    @Retention(RUNTIME)
    public @interface ExternalDependencyFactories {
    }
}
