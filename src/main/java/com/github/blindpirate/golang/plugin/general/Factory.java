package com.github.blindpirate.golang.plugin.general;

public interface Factory<MATERIAL, PRODUCT> {
    PRODUCT produce(MATERIAL material);
}
