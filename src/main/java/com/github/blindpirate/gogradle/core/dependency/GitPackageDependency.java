package com.github.blindpirate.gogradle.core.dependency;

import com.github.blindpirate.gogradle.core.GolangPackageModule;
import com.github.blindpirate.gogradle.core.ProxyPackageModule;

public class GitPackageDependency implements GolangPackageDependency {

    private String url;
    private String commit;

    @Override
    public GolangPackageModule getPackage() {
        return null;
    }
}
