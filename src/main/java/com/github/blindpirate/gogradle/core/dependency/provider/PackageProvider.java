package com.github.blindpirate.gogradle.core.dependency.provider;

import com.github.blindpirate.gogradle.core.dependency.GolangDependency;

/**
 * Determines how to find a package according to its name.
 */
public interface PackageProvider {

    boolean accept(String name);

    GolangDependency parse(String name);

}
