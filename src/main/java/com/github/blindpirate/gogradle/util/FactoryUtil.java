package com.github.blindpirate.gogradle.util;

import com.github.blindpirate.gogradle.general.Factory;
import org.gradle.internal.impldep.org.apache.commons.cli.Option;

import java.util.Optional;

import java.util.List;

public class FactoryUtil {
    public static <MATERIAL, PRODUCT> Optional<PRODUCT> produce(
            List<? extends Factory<? super MATERIAL, PRODUCT>> factories, MATERIAL material) {
        return factories.stream()
                .map(factory -> factory.produce(material))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }
}
