package com.github.blindpirate.gogradle.core.pack;

import com.github.blindpirate.gogradle.core.GolangPackage;
import com.github.blindpirate.gogradle.core.IncompleteGolangPackage;
import com.github.blindpirate.gogradle.util.logging.DebugLog;

import java.util.Optional;

public abstract class AbstractPackagePathResolver implements PackagePathResolver {
    @Override
    @DebugLog
    public Optional<GolangPackage> produce(String packagePath) {
        if (cannotRecognize(packagePath)) {
            return Optional.empty();
        } else if (isIncomplete(packagePath)) {
            return Optional.of(IncompleteGolangPackage.of(packagePath));
        } else {
            return doProduce(packagePath);
        }
    }

    protected abstract Optional<GolangPackage> doProduce(String packagePath);

    protected abstract boolean isIncomplete(String packagePath);

    protected abstract boolean cannotRecognize(String packagePath);
}
