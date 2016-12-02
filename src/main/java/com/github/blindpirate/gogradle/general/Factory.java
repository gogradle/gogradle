package com.github.blindpirate.gogradle.general;

public interface Factory<MATERIAL, PRODUCT> {
    PRODUCT produce(MATERIAL material);
}
