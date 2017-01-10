package com.github.blindpirate.gogradle.core.pack;

import com.github.blindpirate.gogradle.core.GolangPackage;
import com.github.blindpirate.gogradle.core.UnrecognizedGolangPackage;
import com.github.blindpirate.gogradle.util.logging.DebugLog;

import javax.inject.Singleton;
import java.util.Optional;

@Singleton
public class UnrecognizedPackagePathResolver implements PackagePathResolver {
    @Override
    @DebugLog
    public Optional<GolangPackage> produce(String packagePath) {
        return Optional.of(UnrecognizedGolangPackage.of(packagePath));
    }
}
