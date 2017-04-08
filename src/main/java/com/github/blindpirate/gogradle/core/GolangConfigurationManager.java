package com.github.blindpirate.gogradle.core;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

import static com.github.blindpirate.gogradle.core.GolangConfiguration.BUILD;
import static com.github.blindpirate.gogradle.core.GolangConfiguration.TEST;

@Singleton
public class GolangConfigurationManager {
    private final Map<String, GolangConfiguration> configurations = new HashMap<>();

    @Inject
    public GolangConfigurationManager() {
        configurations.put(BUILD, new GolangConfiguration(BUILD));
        configurations.put(TEST, new GolangConfiguration(TEST));
    }

    public GolangConfiguration getByName(String name) {
        return configurations.get(name);
    }
}
