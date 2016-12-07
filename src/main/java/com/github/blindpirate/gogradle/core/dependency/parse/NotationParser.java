package com.github.blindpirate.gogradle.core.dependency.parse;

import com.github.blindpirate.gogradle.core.dependency.GolangDependency;
import com.github.blindpirate.gogradle.general.PickyFactory;

public interface NotationParser extends PickyFactory<Object, GolangDependency> {

    @Override
    boolean accept(Object notation);

    @Override
    GolangDependency produce(Object notation);
}
