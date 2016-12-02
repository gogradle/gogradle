package com.github.blindpirate.gogradle.general;

public interface PickyFactory<MATERIAL, PRODUCT> extends Factory<MATERIAL, PRODUCT> {
    boolean accept(MATERIAL material);
}
