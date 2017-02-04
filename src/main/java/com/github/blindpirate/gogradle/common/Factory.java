package com.github.blindpirate.gogradle.common;

import java.util.Optional;

public interface Factory<MATERIAL, PRODUCT> {
    Optional<PRODUCT> produce(MATERIAL material);

}
