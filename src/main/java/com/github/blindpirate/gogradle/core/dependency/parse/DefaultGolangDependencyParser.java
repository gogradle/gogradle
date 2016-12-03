package com.github.blindpirate.gogradle.core.dependency.parse;

import com.github.blindpirate.gogradle.core.dependency.GolangDependency;
import com.github.blindpirate.gogradle.core.dependency.resolve.DependencyResolutionException;
import com.github.blindpirate.gogradle.general.PickyFactory;
import com.github.blindpirate.gogradle.util.FactoryUtil;

import java.util.ArrayList;
import java.util.List;

public class DefaultGolangDependencyParser implements GolangDependencyParser {

    private List<PickyFactory<Object, GolangDependency>> factories = new ArrayList<>();

    {
        factories.add(new StringNotationParser());
        factories.add(new MapNotationParser());
    }

    @Override
    public GolangDependency parseNotation(Object notaion) {
        try {
            return FactoryUtil.produce(factories, notaion);
        } catch (FactoryUtil.NoViableFactoryException e) {
            throw new DependencyResolutionException("Unsupported dependency notation:" + notaion.getClass());
        }
    }
}
