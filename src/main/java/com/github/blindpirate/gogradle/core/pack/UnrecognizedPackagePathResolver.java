package com.github.blindpirate.gogradle.core.pack;

import com.github.blindpirate.gogradle.core.GolangPackage;
import com.github.blindpirate.gogradle.core.UnrecognizedGolangPackage;
import com.github.blindpirate.gogradle.util.logging.DebugLog;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import javax.inject.Singleton;
import java.util.Optional;

@Singleton
public class UnrecognizedPackagePathResolver implements PackagePathResolver {
    private static final Logger LOGGER = Logging.getLogger(UnrecognizedPackagePathResolver.class);

    @Override
    @DebugLog
    public Optional<GolangPackage> produce(String packagePath) {
        LOGGER.info("Cannot recoginze {}, are you offline now?", packagePath);
        return Optional.of(UnrecognizedGolangPackage.of(packagePath));
    }
}
