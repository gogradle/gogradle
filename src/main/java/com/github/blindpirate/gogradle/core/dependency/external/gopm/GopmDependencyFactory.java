package com.github.blindpirate.gogradle.core.dependency.external.gopm;

import com.github.blindpirate.gogradle.core.GolangPackageModule;
import com.github.blindpirate.gogradle.core.dependency.DependencyHelper;
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet;
import com.github.blindpirate.gogradle.core.dependency.parse.MapNotationParser;
import com.github.blindpirate.gogradle.core.dependency.resolve.ExternalDependencyFactory;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Singleton
public class GopmDependencyFactory extends ExternalDependencyFactory {

    private static final String GOPMFILE = ".gopmfile";
    private static final List<String> FILELIST = Arrays.asList(GOPMFILE);

    @Inject
    private GopmfileParser gopmfileParser;
    @Inject
    private MapNotationParser mapNoationParser;

    @Override
    protected List<String> identityFiles() {
        return FILELIST;
    }

    @Override
    public Optional<GolangDependencySet> doProduce(GolangPackageModule module) {
        File dotGopmfile = module.getRootDir().resolve(GOPMFILE).toFile();
        List<Map<String, Object>> notations = gopmfileParser.parse(dotGopmfile);
        return Optional.of(DependencyHelper.parseMany(notations, mapNoationParser));
    }
}
