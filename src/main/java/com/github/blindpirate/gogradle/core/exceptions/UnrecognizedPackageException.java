package com.github.blindpirate.gogradle.core.exceptions;

import com.github.blindpirate.gogradle.core.UnrecognizedGolangPackage;

public class UnrecognizedPackageException extends RuntimeException {
    private UnrecognizedGolangPackage pkg;

    public UnrecognizedGolangPackage getPkg() {
        return pkg;
    }

    private UnrecognizedPackageException(UnrecognizedGolangPackage pkg) {
        this.pkg = pkg;
    }

    public static UnrecognizedPackageException cannotRecognizePackage(UnrecognizedGolangPackage pkg) {
        return new UnrecognizedPackageException(pkg);
    }
}
