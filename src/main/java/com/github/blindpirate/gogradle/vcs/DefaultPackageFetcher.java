package com.github.blindpirate.gogradle.vcs;

import com.github.blindpirate.gogradle.core.pack.PackageInfo;
import com.github.blindpirate.gogradle.core.pack.PackageNameResolver;
import com.google.common.base.Optional;
import com.google.inject.BindingAnnotation;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static com.github.blindpirate.gogradle.util.VcsUtils.getVcsType;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * https://golang.org/cmd/go/#hdr-Remote_import_paths
 */
@Singleton
public class DefaultPackageFetcher implements PackageFetcher {

    private final PackageNameResolver packageNameResolver;

    @Inject
    public DefaultPackageFetcher(PackageNameResolver packageNameResolver) {
        this.packageNameResolver = packageNameResolver;
    }

    @Override
    public void fetch(String packageName, Path location) {
        PackageInfo packageInfo = packageNameResolver.produce(packageName);
        PackageFetcher actualFetcher = packageInfo.getVcsType()
                .getService(PackageFetcher.class);
        actualFetcher.fetch(packageName, location);
    }
}
