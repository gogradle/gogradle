package com.github.blindpirate.gogradle.core.dependency;

import com.github.blindpirate.gogradle.core.GolangPackage;
import com.github.blindpirate.gogradle.core.GolangPackageModule;
import com.github.blindpirate.gogradle.general.PickyFactory;

/**
 * A factory whose input is a {@link GolangPackageModule} and output is a set of {@link GolangPackageDependency}.
 * <p>
 * This directory is root directory of a {@link GolangPackage}
 */
public interface DependencyFactory extends PickyFactory<GolangPackageModule, GolangDependencySet> {
}
