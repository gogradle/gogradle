package com.github.blindpirate.gogradle.util;

import com.github.blindpirate.gogradle.general.PickyFactory;
import com.google.common.base.Optional;

import java.util.List;

public class FactoryUtil {

    public static class NoViableFactoryException extends RuntimeException {
        public NoViableFactoryException(String message) {
            super(message);
        }
    }

    public static <MATERIAL, PRODUCT> Optional<PRODUCT> produce(List<? extends PickyFactory<MATERIAL, PRODUCT>> factories, MATERIAL material) {
        for (PickyFactory<MATERIAL, PRODUCT> factory : factories) {
            if (factory.accept(material)) {
                return Optional.of(factory.produce(material));
            }
        }
        return Optional.absent();
    }
}
