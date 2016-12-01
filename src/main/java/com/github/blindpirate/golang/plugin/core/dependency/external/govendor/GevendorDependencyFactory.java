package com.github.blindpirate.golang.plugin.core.dependency.external.govendor;

import com.github.blindpirate.golang.plugin.core.dependency.GolangPackageDependency;
import com.github.blindpirate.golang.plugin.core.dependency.tmp.AbstractDependencyFactory;
import org.gradle.api.file.DirectoryTree;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class GevendorDependencyFactory extends AbstractDependencyFactory {
    @Override
    public Set<GolangPackageDependency> produce(DirectoryTree material) {
        return null;
    }

    @Override
    protected List<String> identityFileNames() {
        return Arrays.asList("vendor/vendor.json");
    }
}
