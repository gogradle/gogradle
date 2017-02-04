package com.github.blindpirate.gogradle.core.pack;


import com.github.blindpirate.gogradle.core.GolangPackage;
import com.github.blindpirate.gogradle.common.Factory;

import java.util.Optional;

public interface PackagePathResolver extends Factory<String, GolangPackage> {
    String HTTP = "http://";
    String HTTPS = "https://";

    @Override
    Optional<GolangPackage> produce(String packagePath);
}
