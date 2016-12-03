package com.github.blindpirate.gogradle.util;

import com.github.blindpirate.gogradle.general.PickyFactory;

import java.util.List;

public class FactoryUtil {

    public static class NoViableFactoryException extends RuntimeException {
        public NoViableFactoryException(String message) {
            super(message);
        }
    }

    public static <MATERIAL, PRODUCT> PRODUCT produce(List<? extends PickyFactory<MATERIAL, PRODUCT>> factories,
                                                      MATERIAL material) {
        for (PickyFactory<MATERIAL, PRODUCT> factory : factories) {
            if (factory.accept(material)) {
                return factory.produce(material);
            }
        }
        throw new NoViableFactoryException("cannot produce product!");
    }
}
