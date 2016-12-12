package com.github.blindpirate.gogradle.core.pack;

import com.github.blindpirate.gogradle.core.GolangPackageModule;
import com.github.blindpirate.gogradle.core.dependency.GolangDependency;

public interface DependencyResolver {
     GolangPackageModule resolve(final GolangDependency dependency);
}
