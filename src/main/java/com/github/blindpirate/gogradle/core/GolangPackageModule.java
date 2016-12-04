package com.github.blindpirate.gogradle.core;

import com.github.blindpirate.gogradle.core.dependency.GolangDependency;
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet;
import com.github.blindpirate.gogradle.core.pack.LocalFileSystemModule;

import java.nio.file.Path;

/**
 * A package module is a minimum dependency unit. Actually, it's just a golang package.
 * <p>
 * It represents either a symbol or an existing underlying directory of code
 */
public interface GolangPackageModule extends GolangDependency, GolangPackage {
    /**
     * Dependencies of golang package module = vendor(if any) + declaration(if any) + imports in code
     *
     * @return
     */
    GolangDependencySet getDependencies();

    /**
     * Get the root directory of the underlying code. It may be proxied and expensive.
     *
     * @return
     */
    Path getRootDir();

    /**
     * The time will be used to getPackage conflict finally.
     * In general, the newest version of a package will win.
     * <p>
     * {@link LocalFileSystemModule}
     *
     * @return the update time determined by the package. It may be the last modified time
     * of a file on filesystem or in scm.
     */
    long getUpdateTime();

}
