package com.github.blindpirate.gogradle.util;

import com.github.blindpirate.gogradle.general.Factory;

import java.util.List;
import java.util.Optional;

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
