package com.github.blindpirate.golang.plugin.core;

import com.github.blindpirate.golang.plugin.core.dependency.GolangPackageDependency;

import java.util.Set;

/**
 * A package module is a minimum dependency unit. Actually, it's just a golang package.
 */
public interface GolangPackageModule extends GolangPackage {
    /**
     * Dependencies of golang package module = vendor(if any) + declaration(if any) + imports in code
     *
     * @return
     */
    Set<GolangPackageDependency> getDependencies();
}
