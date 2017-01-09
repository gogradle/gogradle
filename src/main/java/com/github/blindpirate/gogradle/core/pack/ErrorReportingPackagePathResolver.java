package com.github.blindpirate.gogradle.core.pack;

import com.github.blindpirate.gogradle.core.GolangPackage;
import com.github.blindpirate.gogradle.core.exceptions.PackageResolutionException;
import com.github.blindpirate.gogradle.util.logging.DebugLog;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;

@Singleton
public class ErrorReportingPackagePathResolver implements PackagePathResolver {
    private final OptionalPackagePathResolver optionalPackagePathResolver;

    @Inject
    public ErrorReportingPackagePathResolver(OptionalPackagePathResolver optionalPackagePathResolver) {
        this.optionalPackagePathResolver = optionalPackagePathResolver;
    }

    @Override
    @DebugLog
    public Optional<GolangPackage> produce(String packagePath) {
        Optional<GolangPackage> result = optionalPackagePathResolver.produce(packagePath);
        reportErrorIfResolutionFailed(packagePath, result);
        return result;
    }

    private void reportErrorIfResolutionFailed(String packagePath, Optional<GolangPackage> result) {
        if (!result.isPresent()) {
            throw PackageResolutionException.cannotResolvePath(packagePath);
        }
    }

}
