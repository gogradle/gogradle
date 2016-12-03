package com.github.blindpirate.gogradle.core.dependency.parse;

import com.github.blindpirate.gogradle.core.dependency.GolangDependency;
import com.github.blindpirate.gogradle.general.PickyFactory;

public class StringNotationParser implements PickyFactory<Object, GolangDependency> {
    @Override
    public GolangDependency produce(Object o) {
        return null;
    }

    @Override
    public boolean accept(Object o) {
        return o instanceof String;
    }
}
