package com.github.blindpirate.gogradle.util;

import com.github.blindpirate.gogradle.general.Factory;
import java.util.Optional;

import java.util.List;

public class FactoryUtil {
    public static <MATERIAL, PRODUCT> Optional<PRODUCT> produce(
            List<? extends Factory<? super MATERIAL, PRODUCT>> factories, MATERIAL material) {
        for (Factory<? super MATERIAL, PRODUCT> factory : factories) {
            Optional<PRODUCT> result = factory.produce(material);
            if (result.isPresent()) {
                return result;
            }
        }
        return Optional.empty();
    }
}
