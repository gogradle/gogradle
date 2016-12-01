package com.github.blindpirate.golang.plugin.general;

public interface PickyFactory<MATERIAL, PRODUCT> extends Factory<MATERIAL, PRODUCT> {
    boolean accept(MATERIAL material);
}
