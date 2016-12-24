package com.github.blindpirate.gogradle.core.pack;


import com.github.blindpirate.gogradle.general.Factory;

import java.util.Optional;

public interface PackageNameResolver extends Factory<String, PackageInfo> {
    String HTTP = "http://";
    String HTTPS = "https://";

    @Override
    Optional<PackageInfo> produce(String packageName);
}
