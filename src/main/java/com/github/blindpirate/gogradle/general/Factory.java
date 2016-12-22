package com.github.blindpirate.gogradle.general;

import com.google.common.base.Optional;

public interface Factory<MATERIAL, PRODUCT> {
    Optional<PRODUCT> produce(MATERIAL material);

}
