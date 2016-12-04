package com.github.blindpirate.gogradle.core.dependency.parse;

import com.github.blindpirate.gogradle.core.dependency.GolangDependency;
import com.github.blindpirate.gogradle.core.dependency.provider.GithubPackageProvider;
import com.github.blindpirate.gogradle.core.dependency.provider.PackageProvider;
import com.github.blindpirate.gogradle.core.dependency.resolve.DependencyResolutionException;
import com.github.blindpirate.gogradle.general.PickyFactory;

import java.util.ArrayList;
import java.util.List;

public class StringNotationParser implements PickyFactory<Object, GolangDependency> {

    private List<PackageProvider> providers = new ArrayList<>();

    {
        providers.add(new GithubPackageProvider());
    }

    @Override
    public GolangDependency produce(Object o) {
        for (PackageProvider provider : providers) {
            if (provider.accept((String) o)) {
                return provider.parse((String) o);
            }
        }
        throw new DependencyResolutionException("Cannot parse:" + o);
    }

    @Override
    public boolean accept(Object o) {
        return o instanceof String;
    }
}
