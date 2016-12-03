package com.github.blindpirate.gogradle.core.dependency;

import com.github.blindpirate.gogradle.core.GolangPackageModule;
import com.github.blindpirate.gogradle.core.ProxyPackageModule;

public class GitPackageDependency extends ScmPackageDependency {

    private String url;
    private String commit;
    private String tag;

    @Override
    public String getVersion() {
        return commit;
    }

    @Override
    public GolangPackageModule getPackage() {
        return null;
    }
}
